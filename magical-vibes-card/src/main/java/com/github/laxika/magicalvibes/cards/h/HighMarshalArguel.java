package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.AllConditions;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanent;
import com.github.laxika.magicalvibes.model.effect.ConditionalReplacementEffect;
import com.github.laxika.magicalvibes.model.effect.ConjureCardToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.TransformCreatedPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsEnchantmentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNamedPredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "YLCI", collectorNumber = "24")
public class HighMarshalArguel extends Card {

    public HighMarshalArguel() {
        AllConditions controlsArguelsBloodFastAndTemple = new AllConditions(List.of(
                new ControlsPermanent(new PermanentAllOfPredicate(List.of(
                        new PermanentIsEnchantmentPredicate(),
                        new PermanentNamedPredicate("Arguel's Blood Fast")))),
                new ControlsPermanent(new PermanentAllOfPredicate(List.of(
                        new PermanentIsLandPredicate(),
                        new PermanentNamedPredicate("Temple of Aclazotz"))))));

        CreateTokenEffect vampireDemons = new CreateTokenEffect(
                2, "Vampire Demon", 4, 3, CardColor.WHITE,
                Set.of(CardColor.WHITE, CardColor.BLACK),
                List.of(CardSubtype.VAMPIRE, CardSubtype.DEMON),
                Set.of(Keyword.FLYING), Set.of());

        addEffect(EffectSlot.ON_DEATH, new ConditionalReplacementEffect(
                controlsArguelsBloodFastAndTemple,
                SequenceEffect.of(
                        new ConjureCardToBattlefieldEffect("Arguel's Blood Fast"),
                        new MayEffect(new TransformCreatedPermanentEffect(), "Transform it?")),
                vampireDemons));
    }
}
