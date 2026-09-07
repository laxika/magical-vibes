package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;
import java.util.Map;
import java.util.Set;

@CardRegistration(set = "FIN", collectorNumber = "145")
public class MysidianElder extends Card {

    public MysidianElder() {
        // When this creature enters, create a 0/1 black Wizard creature token with
        // "Whenever you cast a noncreature spell, this token deals 1 damage to each opponent."
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new CreateTokenEffect(
                CardType.CREATURE, 1, "Wizard", 0, 1,
                CardColor.BLACK, null, List.of(CardSubtype.WIZARD), Set.of(), Set.of(),
                false, false,
                Map.of(EffectSlot.ON_CONTROLLER_CASTS_SPELL, new SpellCastTriggerEffect(
                        new CardNotPredicate(new CardTypePredicate(CardType.CREATURE)),
                        List.of(new DealDamageToPlayersEffect(1, DamageRecipient.EACH_OPPONENT)))),
                List.of(), false, false, false, 0, Set.of()));
    }
}
