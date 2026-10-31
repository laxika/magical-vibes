package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.amount.SourceIntensity;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.InitializeSourceIntensityEffect;
import com.github.laxika.magicalvibes.model.effect.IntensifyCardsOfSubtypeEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeRecipient;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

import java.util.List;

@CardRegistration(set = "YLCI", collectorNumber = "12")
public class MycelicBallad extends Card {

    public MycelicBallad() {
        addSpellEffect(new InitializeSourceIntensityEffect(2));
        addSpellEffect(new SacrificePermanentsEffect(
                new SourceIntensity(),
                new PermanentAllOfPredicate(List.of(new PermanentIsCreaturePredicate())),
                SacrificeRecipient.EACH_PLAYER).withSimultaneousChoices());
        addSpellEffect(new GainLifeEffect(new SourceIntensity()));
        addSpellEffect(new IntensifyCardsOfSubtypeEffect(CardSubtype.CHORUS));
    }
}
