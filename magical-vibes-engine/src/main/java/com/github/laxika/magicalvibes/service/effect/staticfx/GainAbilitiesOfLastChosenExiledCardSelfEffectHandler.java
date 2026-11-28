package com.github.laxika.magicalvibes.service.effect.staticfx;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.EffectRegistration;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CardSubject;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfImprintedCardEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutExiledCardsIntoOwnersGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.GainAbilitiesOfLastChosenExiledCardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantTriggeredAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.ExileGraveyardCardsEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetCreatureCardFromGraveyardPutCounterOnSourceEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPermanentAndTrackWithSourceEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPermanentUntilSourceLeavesEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardExiledWithSourceToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.service.effect.StaticBonusAccumulator;
import com.github.laxika.magicalvibes.service.effect.StaticEffectContext;
import org.springframework.stereotype.Component;


@Component
public class GainAbilitiesOfLastChosenExiledCardSelfEffectHandler implements StaticEffectHandlerBean {

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return GainAbilitiesOfLastChosenExiledCardEffect.class;
    }

    @Override
    public boolean selfOnly() {
        return true;
    }

    @Override
    public void apply(StaticEffectContext context, CardEffect effect, StaticBonusAccumulator accumulator) {
        Card chosenCard = context.source().getLastChosenExiledCard();
        if (chosenCard == null) return;
        if (context.gameData() != null && context.gameData().getCardsExiledByPermanent(context.source().getId())
                .stream().noneMatch(c -> c.getId().equals(chosenCard.getId()))) {
            return;
        }

        String link = "gained:" + chosenCard.getId();
        for (ActivatedAbility ability : ManaAbilities.withIntrinsic(chosenCard)) {
            accumulator.addActivatedAbility(ability.withEffects(ability.getEffects().stream()
                    .map(gainedEffect -> linkGainedEffect(gainedEffect, link)).toList()));
        }


        for (TriggeredAbility ability : chosenCard.getTriggeredAbilities()) {
            accumulator.addGrantedEffect(new GrantTriggeredAbilityEffect(linkGainedTrigger(ability, link),
                    Subjects.source()));
        }
    }

    /** {@code ability} gained with the others of its card: its effects linked as {@link #linkGainedEffect} links them. */
    static TriggeredAbility linkGainedTrigger(TriggeredAbility ability, String link) {
        return ability.withEffects(ability.effects().stream()
                .map(registration -> new EffectRegistration(linkGainedEffect(registration.effect(), link),
                        registration.simultaneousWithPrevious()))
                .toList());
    }

    /**
     * Whether {@code ability} has an imprint exile, which a borrower's copy records under the gained link: the abilities
     * gained with it that read "the exiled card" then read that link's card (CR 607.5).
     */
    static boolean hasImprintExile(TriggeredAbility ability) {
        return ability.effects().stream().anyMatch(registration -> hasImprintExile(registration.effect()));
    }

    private static boolean hasImprintExile(CardEffect effect) {
        return switch (effect) {
            case ExileGraveyardCardsEffect exile -> exile.imprint();
            case MayEffect may -> hasImprintExile(may.wrapped());
            case SequenceEffect sequence -> sequence.steps().stream()
                    .anyMatch(GainAbilitiesOfLastChosenExiledCardSelfEffectHandler::hasImprintExile);
            case ConditionalEffect conditional -> hasImprintExile(conditional.wrapped());
            default -> false;
        };
    }

    /**
     * "Cards exiled with [the source]" of a gained ability, as the gained link's cards only: {@code cards} with each
     * unlinked {@link CardSubject.ExiledWithSource} given {@code link} (CR 607.5).
     */
    private static CardSubject relinkExiledWithSource(CardSubject cards, String link) {
        return switch (cards) {
            case CardSubject.ExiledWithSource exiled when exiled.abilityLink() == null ->
                    new CardSubject.ExiledWithSource(exiled.owners(), exiled.filter(), link);
            case CardSubject.Except except -> new CardSubject.Except(relinkExiledWithSource(except.cards(), link),
                    relinkExiledWithSource(except.excluded(), link));
            default -> cards;
        };
    }

    /**
     * An exile ability gained from the chosen card tracks its cards under a link of its own, and the gained
     * ability that returns them looks only at that link, so neither touches cards exiled by the
     * gaining permanent's own abilities; an exile that records a constant link of its printed ability (Patchwork
     * Crawler's) records the gained link instead. A gained imprint exile doesn't imprint: the gaining permanent's own
     * imprint readers ("all abilities of the exiled card") read only what its own imprint exiled (CR 607.2a); it
     * records the gained link, and the gained abilities reading "cards exiled with it" (Mimic Vat's "each other card")
     * or "the exiled card" (its token copy) read that link, so a pair gained together stays linked (CR 607.5). The
     * effects a "may", a sequence or an "if" wraps are linked too.
     */
    static CardEffect linkGainedEffect(CardEffect effect, String link) {
        if (effect instanceof MayEffect may) {
            return new MayEffect(linkGainedEffect(may.wrapped(), link), may.prompt(),
                    may.elseEffect() == null ? null : linkGainedEffect(may.elseEffect(), link), may.choicePlayer(),
                    may.choicePlayerId(), may.targetOfTheirChoice());
        }
        if (effect instanceof SequenceEffect sequence) {
            return sequence.withSteps(sequence.steps().stream().map(step -> linkGainedEffect(step, link)).toList());
        }
        if (effect instanceof ConditionalEffect conditional) {
            return new ConditionalEffect(conditional.condition(), linkGainedEffect(conditional.wrapped(), link));
        }
        if (effect instanceof PutExiledCardsIntoOwnersGraveyardEffect put) {
            return new PutExiledCardsIntoOwnersGraveyardEffect(relinkExiledWithSource(put.cards(), link));
        }
        if (effect instanceof CreateTokenCopyOfImprintedCardEffect copy && !copy.requiresImprintedXCost()) {
            return copy.ofCardExiledBy(link);
        }
        if (effect instanceof ExileTargetPermanentAndTrackWithSourceEffect) {
            return new ExileTargetPermanentAndTrackWithSourceEffect(link);
        }
        if (effect instanceof ReturnCardExiledWithSourceToBattlefieldEffect returnEffect) {
            return returnEffect.withAbilityLink(link);
        }
        if (effect instanceof ExileTargetCreatureCardFromGraveyardPutCounterOnSourceEffect exile) {
            return exile.withAbilityLink(link);
        }
        if (effect instanceof ExileGraveyardCardsEffect exile && exile.imprint()) {
            return exile.notImprinting().withAbilityLink(link);
        }
        if (effect instanceof ExileTargetPermanentUntilSourceLeavesEffect exile && exile.imprint()) {
            return new ExileTargetPermanentUntilSourceLeavesEffect(false, exile.targetPredicate(),
                    exile.exileIfSourceAlreadyLeft(), exile.returnsOnSourceLeavesTrigger(),
                    exile.controllerMayCastWithAnyMana(), exile.permanents());
        }
        return effect;
    }
}
