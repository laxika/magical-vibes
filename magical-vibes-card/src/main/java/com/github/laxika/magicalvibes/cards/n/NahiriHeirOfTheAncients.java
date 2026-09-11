package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.amount.Scaled;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokensAndAttachEquipmentEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureOrPlaneswalkerEffect;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "ZNR", collectorNumber = "230")
public class NahiriHeirOfTheAncients extends Card {

    public NahiriHeirOfTheAncients() {
        CreateTokenEffect korWarrior = new CreateTokenEffect(
                "Kor Warrior", 1, 1, CardColor.WHITE,
                List.of(CardSubtype.KOR, CardSubtype.WARRIOR), Set.of(), Set.of());
        addActivatedAbility(new ActivatedAbility(
                +1,
                List.of(new CreateTokensAndAttachEquipmentEffect(korWarrior)),
                "+1: Create a 1/1 white Kor Warrior creature token. You may attach an Equipment you control to it."
        ));

        addActivatedAbility(new ActivatedAbility(
                -2,
                List.of(LookAtTopCardsEffect.mayRevealOneToHandRestOnBottomRandom(6,
                        new CardAnyOfPredicate(List.of(
                                new CardSubtypePredicate(CardSubtype.WARRIOR),
                                new CardSubtypePredicate(CardSubtype.EQUIPMENT))))),
                "−2: Look at the top six cards of your library. You may reveal a Warrior or Equipment card from among them and put it into your hand. Put the rest on the bottom of your library in a random order."
        ));

        PermanentCount equipment = new PermanentCount(
                new PermanentHasSubtypePredicate(CardSubtype.EQUIPMENT), CountScope.CONTROLLER);
        addActivatedAbility(new ActivatedAbility(
                -3,
                List.of(new DealDamageToTargetCreatureOrPlaneswalkerEffect(new Scaled(equipment, 2))),
                "−3: Nahiri deals damage to target creature or planeswalker equal to twice the number of Equipment you control."
        ));
    }
}
