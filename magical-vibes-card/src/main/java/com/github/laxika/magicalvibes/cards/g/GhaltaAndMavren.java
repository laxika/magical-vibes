package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.GreatestPowerAmongControlled;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsAttackingPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourceCardPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

import java.util.List;
import java.util.Map;
import java.util.Set;

@CardRegistration(set = "MOM", collectorNumber = "225")
public class GhaltaAndMavren extends Card {

    private static final String DINOSAUR_MODE =
            "Create a tapped and attacking X/X green Dinosaur creature token with trample.";
    private static final String VAMPIRE_MODE =
            "Create X 1/1 white Vampire creature tokens with lifelink.";

    public GhaltaAndMavren() {
        PermanentAllOfPredicate otherAttackingCreatures = new PermanentAllOfPredicate(List.of(
                new PermanentIsAttackingPredicate(),
                new PermanentNotPredicate(new PermanentIsSourceCardPredicate())));
        GreatestPowerAmongControlled greatestOtherAttackingPower =
                new GreatestPowerAmongControlled(otherAttackingCreatures);
        PermanentCount otherAttackers = new PermanentCount(
                new PermanentIsAttackingPredicate(), CountScope.CONTROLLER, true);

        CreateTokenEffect dinosaur = new CreateTokenEffect(
                CardType.CREATURE,
                new Fixed(1),
                "Dinosaur",
                greatestOtherAttackingPower,
                greatestOtherAttackingPower,
                CardColor.GREEN,
                null,
                List.of(CardSubtype.DINOSAUR),
                Set.of(Keyword.TRAMPLE),
                Set.of(),
                true,
                false,
                Map.of(),
                List.of(),
                false,
                false,
                false,
                0,
                Set.of());
        CreateTokenEffect vampire = new CreateTokenEffect(
                otherAttackers,
                "Vampire",
                1,
                1,
                CardColor.WHITE,
                List.of(CardSubtype.VAMPIRE),
                Set.of(Keyword.LIFELINK),
                Set.of());

        addEffect(EffectSlot.ON_ALLY_CREATURES_ATTACK, new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption(DINOSAUR_MODE, dinosaur),
                new ChooseOneEffect.ChooseOneOption(VAMPIRE_MODE, vampire))));
    }
}
