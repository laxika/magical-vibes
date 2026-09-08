package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeRecipient;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class AwakenTheBloodAvatar extends Card {

    public AwakenTheBloodAvatar() {
        addEffect(EffectSlot.SPELL, new SacrificePermanentsEffect(
                1, new PermanentIsCreaturePredicate(), SacrificeRecipient.EACH_OPPONENT));
        addEffect(EffectSlot.SPELL, new CreateTokenEffect(
                CardType.CREATURE, 1, "Avatar", 3, 6,
                CardColor.BLACK, Set.of(CardColor.BLACK, CardColor.RED),
                List.of(CardSubtype.AVATAR), Set.of(Keyword.HASTE), Set.of(),
                false, false,
                Map.of(EffectSlot.ON_ATTACK,
                        new DealDamageToPlayersEffect(3, DamageRecipient.EACH_OPPONENT)),
                List.of(), false, false, false, 0, Set.of()));
    }
}
