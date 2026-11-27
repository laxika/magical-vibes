package com.github.laxika.magicalvibes.service.effect.staticfx;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CardSubject;
import com.github.laxika.magicalvibes.model.effect.GainActivatedAndTriggeredAbilitiesOfExiledCardsEffect;
import com.github.laxika.magicalvibes.model.effect.GrantTriggeredAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.service.effect.StaticBonusAccumulator;
import com.github.laxika.magicalvibes.service.effect.StaticEffectContext;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class GainActivatedAndTriggeredAbilitiesOfExiledCardsSelfEffectHandler
        implements StaticEffectHandlerBean {

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return GainActivatedAndTriggeredAbilitiesOfExiledCardsEffect.class;
    }

    @Override
    public boolean selfOnly() {
        return true;
    }

    @Override
    public void apply(StaticEffectContext context, CardEffect effect, StaticBonusAccumulator accumulator) {
        for (Card card : exiledByImprint(context)) {
            String link = "gained:" + card.getId();
            for (ActivatedAbility ability : ManaAbilities.withIntrinsic(card)) {
                accumulator.addActivatedAbility(
                        GainActivatedAbilitiesOfExiledCardsSelfEffectHandler.borrowedAbility(ability, link));
            }


            for (TriggeredAbility ability : card.getTriggeredAbilities()) {

                accumulator.addGrantedEffect(new GrantTriggeredAbilityEffect(ability, Subjects.source()));

            }
        }
    }

    /**
     * "The exiled card": the cards the source's imprint exiled (CR 607.2a), not one a borrowed exile ability exiled
     * with it.
     */
    private static List<Card> exiledByImprint(StaticEffectContext context) {
        synchronized (context.gameData().exiledCards) {
            return context.gameData().exiledCards.stream()
                    .filter(exiled -> context.sourceId().equals(exiled.sourcePermanentId())
                            && CardSubject.ExiledWithSource.IMPRINT.equals(exiled.abilityLink()))
                    .map(ExiledCardEntry::card)
                    .toList();
        }
    }
}
