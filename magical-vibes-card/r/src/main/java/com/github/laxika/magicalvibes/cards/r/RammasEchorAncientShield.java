package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.NthSpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasKeywordPredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "LTC", collectorNumber = "505")
@CardRegistration(set = "LTC", collectorNumber = "549")
public class RammasEchorAncientShield extends Card {

    private static final CreateTokenEffect WALL_TOKEN = new CreateTokenEffect(
            "Wall", 0, 3, CardColor.WHITE,
            List.of(CardSubtype.WALL), Set.of(Keyword.DEFENDER), Set.of());

    public RammasEchorAncientShield() {
        // Whenever you cast your second spell each turn, draw a card, then create a 0/3 white
        // Wall creature token with defender.
        addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL, new NthSpellCastTriggerEffect(
                2,
                List.of(new DrawCardEffect(), WALL_TOKEN)
        ));

        // At the beginning of combat on your turn, creatures you control with defender gain
        // exalted until end of turn.
        addEffect(EffectSlot.BEGINNING_OF_COMBAT_TRIGGERED,
                new GrantKeywordEffect(Keyword.EXALTED, GrantScope.OWN_CREATURES,
                        new PermanentHasKeywordPredicate(Keyword.DEFENDER)));
    }
}
