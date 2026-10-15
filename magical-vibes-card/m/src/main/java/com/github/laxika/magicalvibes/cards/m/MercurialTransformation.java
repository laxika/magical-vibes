package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AnimatePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneForTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.LosesAllAbilitiesEffect;
import com.github.laxika.magicalvibes.model.effect.TargetCreatureBecomesSubtypeUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "STX", collectorNumber = "47")
public class MercurialTransformation extends Card {

    private static final String FROG_MODE = "Become a blue Frog creature with base power and toughness 1/1";
    private static final String OCTOPUS_MODE = "Become a blue Octopus creature with base power and toughness 4/4";

    public MercurialTransformation() {
        target(TargetFilters.nonlandPermanent())
                .addEffect(EffectSlot.SPELL,
                        new LosesAllAbilitiesEffect(GrantScope.TARGET, EffectDuration.UNTIL_END_OF_TURN))
                .addEffect(EffectSlot.SPELL, new ChooseOneForTargetPermanentEffect(List.of(
                        new ChooseOneEffect.ChooseOneOption(FROG_MODE, List.of(
                                animate(1, 1, CardSubtype.FROG),
                                new TargetCreatureBecomesSubtypeUntilEndOfTurnEffect(CardSubtype.FROG))),
                        new ChooseOneEffect.ChooseOneOption(OCTOPUS_MODE, List.of(
                                animate(4, 4, CardSubtype.OCTOPUS),
                                new TargetCreatureBecomesSubtypeUntilEndOfTurnEffect(CardSubtype.OCTOPUS))))));
    }

    private static AnimatePermanentsEffect animate(int power, int toughness, CardSubtype subtype) {
        return new AnimatePermanentsEffect(power, toughness, List.of(subtype), Set.of(), CardColor.BLUE,
                Set.of(), GrantScope.TARGET, EffectDuration.UNTIL_END_OF_TURN);
    }
}
