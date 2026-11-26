package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsEnchantmentPredicate;
import com.github.laxika.magicalvibes.model.amount.AmountThisWay;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.effect.EachPlayerCreatesTokenEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "RNA", collectorNumber = "134")
public class RampageOfTheClans extends Card {

    public RampageOfTheClans() {
        addSpellEffect(DestroyTargetPermanentEffect.all(
                new PermanentAnyOfPredicate(List.of(
                        new PermanentIsArtifactPredicate(),
                        new PermanentIsEnchantmentPredicate()))));
        addSpellEffect(new EachPlayerCreatesTokenEffect(new CreateTokenEffect(
                new AmountThisWay(StepKind.DESTROY).controlledByYou(), "Centaur", 3, 3, CardColor.GREEN,
                List.of(CardSubtype.CENTAUR), Set.of(), Set.of())));
    }
}
