package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeRecipient;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "WHO", collectorNumber = "67")
@CardRegistration(set = "WHO", collectorNumber = "372")
public class DoomsdayConfluence extends Card {

    public DoomsdayConfluence() {
        addEffect(EffectSlot.SPELL, ChooseOneEffect.withRepeatedModesForX(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Each player sacrifices a nonartifact creature of their choice",
                        new SacrificePermanentsEffect(
                                1,
                                new PermanentAllOfPredicate(List.of(
                                        new PermanentIsCreaturePredicate(),
                                        new PermanentNotPredicate(new PermanentIsArtifactPredicate()))),
                                SacrificeRecipient.EACH_PLAYER)),
                new ChooseOneEffect.ChooseOneOption(
                        "Create a 3/3 black Dalek artifact creature token with menace",
                        new CreateTokenEffect("Dalek", 3, 3, CardColor.BLACK,
                                List.of(CardSubtype.DALEK), Set.of(Keyword.MENACE), Set.of(CardType.ARTIFACT))),
                new ChooseOneEffect.ChooseOneOption(
                        "Each opponent discards a card",
                        new DiscardEffect(1, DiscardRecipient.EACH_OPPONENT))
        )));
    }
}
