package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsAttackingPredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "40K", collectorNumber = "159")
public class InquisitorialRosette extends Card {

    public InquisitorialRosette() {
        // Whenever equipped creature attacks, create a 2/2 white Astartes Warrior creature token
        // with vigilance that's attacking. Then attacking creatures gain menace until end of turn.
        addEffect(EffectSlot.ON_ATTACK, SequenceEffect.of(
                new CreateTokenEffect(
                        1, "Astartes Warrior", 2, 2, CardColor.WHITE,
                        List.of(CardSubtype.ASTARTES, CardSubtype.WARRIOR),
                        Set.of(Keyword.VIGILANCE), true, false),
                new GrantKeywordEffect(
                        Keyword.MENACE, GrantScope.ALL_CREATURES, new PermanentIsAttackingPredicate())
        ));

        // Equip {3}
        addActivatedAbility(new EquipActivatedAbility("{3}"));
    }
}
