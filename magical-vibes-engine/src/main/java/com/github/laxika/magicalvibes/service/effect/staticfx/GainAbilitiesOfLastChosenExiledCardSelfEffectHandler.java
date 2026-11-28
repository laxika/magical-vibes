package com.github.laxika.magicalvibes.service.effect.staticfx;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
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

            accumulator.addGrantedEffect(new GrantTriggeredAbilityEffect(ability, Subjects.source()));

        }
    }

    /**
     * An exile ability gained from the chosen card tracks its cards under a link of its own, and the gained
     * ability that returns them looks only at that link, so neither touches cards exiled by the
     * gaining permanent's own abilities; an exile that records a constant link of its printed ability (Patchwork
     * Crawler's) records the gained link instead. A gained imprint exile doesn't imprint: the gaining permanent's own
     * imprint readers ("all abilities of the exiled card") read only what its own imprint exiled (CR 607.2a).
     */
    static CardEffect linkGainedEffect(CardEffect effect, String link) {
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
            return exile.notImprinting();
        }
        if (effect instanceof ExileTargetPermanentUntilSourceLeavesEffect exile && exile.imprint()) {
            return new ExileTargetPermanentUntilSourceLeavesEffect(false, exile.targetPredicate(),
                    exile.exileIfSourceAlreadyLeft(), exile.returnsOnSourceLeavesTrigger(),
                    exile.controllerMayCastWithAnyMana(), exile.permanents());
        }
        return effect;
    }
}
