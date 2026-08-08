package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.SetPowerToughnessToAmountEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

import java.util.List;
import java.util.Map;
import java.util.Set;

@CardRegistration(set = "DGM", collectorNumber = "114")
public class VoiceOfResurgence extends Card {

    public VoiceOfResurgence() {
        // Whenever an opponent casts a spell during your turn and when this creature dies,
        // create a green and white Elemental creature token with "This token's power and
        // toughness are each equal to the number of creatures you control."
        DynamicAmount creaturesYouControl =
                new PermanentCount(new PermanentIsCreaturePredicate(), CountScope.CONTROLLER);
        CreateTokenEffect token = new CreateTokenEffect(
                CardType.CREATURE, 1, "Elemental", 0, 0,
                CardColor.GREEN, Set.of(CardColor.GREEN, CardColor.WHITE),
                List.of(CardSubtype.ELEMENTAL), Set.of(), Set.of(),
                false, false,
                Map.of(EffectSlot.STATIC,
                        new SetPowerToughnessToAmountEffect(creaturesYouControl, creaturesYouControl)),
                List.of(), false, false, false, 0, Set.of()
        );

        addEffect(EffectSlot.ON_OPPONENT_CASTS_SPELL,
                SpellCastTriggerEffect.duringYourTurn(null, List.of(token)));
        addEffect(EffectSlot.ON_DEATH, token);
    }
}
