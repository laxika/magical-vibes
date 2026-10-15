package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardRestrictedManaEffect;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsEffect;
import com.github.laxika.magicalvibes.model.effect.ManaRestriction;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;

import java.util.List;

@CardRegistration(set = "BLB", collectorNumber = "256")
public class LupinflowerVillage extends Card {

    public LupinflowerVillage() {
        addActivatedAbility(ManaAbilities.tapFor(ManaColor.COLORLESS));
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new AwardRestrictedManaEffect(
                        ManaColor.WHITE, 1, new ManaRestriction.CreatureSpells())),
                "{T}: Add {W}. Spend this mana only to cast a creature spell."));
        addActivatedAbility(new ActivatedAbility(
                true,
                "{1}{W}",
                List.of(
                        new SacrificeSelfCost(),
                        LookAtTopCardsEffect.mayRevealOneToHandRestOnBottomRandom(
                                6,
                                new CardAnyOfPredicate(List.of(
                                        new CardSubtypePredicate(CardSubtype.BAT),
                                        new CardSubtypePredicate(CardSubtype.BIRD),
                                        new CardSubtypePredicate(CardSubtype.MOUSE),
                                        new CardSubtypePredicate(CardSubtype.RABBIT))))),
                "{1}{W}, {T}, Sacrifice this land: Look at the top six cards of your library. You may reveal a Bat, Bird, Mouse, or Rabbit card from among them and put it into your hand. Put the rest on the bottom of your library in a random order."));
    }
}
