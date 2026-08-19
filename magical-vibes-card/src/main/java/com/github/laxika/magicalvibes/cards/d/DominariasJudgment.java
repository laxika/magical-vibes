package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanent;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantProtectionFromColorUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

@CardRegistration(set = "PLS", collectorNumber = "4")
public class DominariasJudgment extends Card {

    public DominariasJudgment() {
        addConditionalProtection(CardSubtype.PLAINS, CardColor.WHITE);
        addConditionalProtection(CardSubtype.ISLAND, CardColor.BLUE);
        addConditionalProtection(CardSubtype.SWAMP, CardColor.BLACK);
        addConditionalProtection(CardSubtype.MOUNTAIN, CardColor.RED);
        addConditionalProtection(CardSubtype.FOREST, CardColor.GREEN);
    }

    private void addConditionalProtection(CardSubtype landSubtype, CardColor color) {
        addEffect(EffectSlot.SPELL, new ConditionalEffect(
                new ControlsPermanent(new PermanentHasSubtypePredicate(landSubtype)),
                new GrantProtectionFromColorUntilEndOfTurnEffect(color, GrantScope.OWN_CREATURES)));
    }
}
