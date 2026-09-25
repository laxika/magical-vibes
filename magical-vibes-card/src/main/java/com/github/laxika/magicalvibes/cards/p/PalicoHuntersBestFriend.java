package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.OracleData;
import com.github.laxika.magicalvibes.model.effect.AttachSelectedEquipmentToCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;

import java.util.List;
import java.util.Set;

/** Leaked/cancelled Monster Hunter Secret Lair bonus card. */
@CardRegistration(set = "SLD", collectorNumber = "2257")
@CardRegistration(set = "SLD", collectorNumber = "2259")
@CardRegistration(set = "SLD", collectorNumber = "2261")
@CardRegistration(set = "SLD", collectorNumber = "2262")
@CardRegistration(set = "SLD", collectorNumber = "2264")
@CardRegistration(set = "SLD", collectorNumber = "2266")
public class PalicoHuntersBestFriend extends Card {

    /* SLD #2257 is not in the public oracle feeds, so retain the leaked printing's oracle data. */
    static {
        Card.registerEmbeddedOracle("PalicoHuntersBestFriend", new OracleData(
                "Palico, Hunter's Best Friend",
                CardType.CREATURE,
                Set.of(),
                "{3}{W}",
                CardColor.WHITE,
                List.of(CardColor.WHITE),
                List.of(CardColor.WHITE),
                Set.of(),
                List.of(CardSubtype.CAT, CardSubtype.KNIGHT),
                "Flying\nWhenever this creature attacks, look at the top six cards of your library. "
                        + "You may put an Aura or Equipment card from among them onto the battlefield. "
                        + "If an Equipment is put onto the battlefield this way, you may attach it to a "
                        + "creature you control. Put the rest of those cards on the bottom of your library "
                        + "in a random order.",
                3,
                3,
                Set.of(Keyword.FLYING),
                null,
                null,
                null));
    }

    public PalicoHuntersBestFriend() {
        LookAtTopCardsEffect search =
                LookAtTopCardsEffect.mayPutMatchingOntoBattlefieldRestOnBottomRandom(
                        6,
                        new CardAnyOfPredicate(List.of(
                                new CardSubtypePredicate(CardSubtype.AURA),
                                new CardSubtypePredicate(CardSubtype.EQUIPMENT))),
                        new AttachSelectedEquipmentToCreatureEffect());
        addEffect(EffectSlot.ON_ATTACK, search);
    }
}
