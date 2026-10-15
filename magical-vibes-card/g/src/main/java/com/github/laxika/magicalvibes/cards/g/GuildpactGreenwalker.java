package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.ChooseCardFromHandAndApplyPerpetualPowerToughnessAndIncorporationEffect;
import com.github.laxika.magicalvibes.model.effect.CounterUnlessPaysEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.GrantTriggeredAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsMulticoloredPredicate;

@CardRegistration(set = "YMKM", collectorNumber = "17")
public class GuildpactGreenwalker extends Card {

    public GuildpactGreenwalker() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new MayEffect(
                new ChooseCardFromHandAndApplyPerpetualPowerToughnessAndIncorporationEffect(
                        new CardTypePredicate(CardType.CREATURE), 4, 4, "{1}{G}"),
                "Choose a creature card in your hand to perpetually incorporate {1}{G} and get +4/+4?"));

        var multicoloredCreature = new PermanentIsMulticoloredPredicate();
        addEffect(EffectSlot.STATIC, new GrantKeywordEffect(
                Keyword.WARD, GrantScope.OWN_CREATURES, multicoloredCreature));
        addEffect(EffectSlot.STATIC, new GrantTriggeredAbilityEffect(
                EffectSlot.ON_BECOMES_TARGET_OF_OPPONENT_SPELL,
                new CounterUnlessPaysEffect(2), GrantScope.OWN_CREATURES, multicoloredCreature));
    }
}
