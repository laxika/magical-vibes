package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.DistinctPermanentNamesAmongControlled;
import com.github.laxika.magicalvibes.model.condition.AttackedWithCommanderThisTurn;
import com.github.laxika.magicalvibes.model.effect.AllowCastFromCardsExiledWithSourceEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardsToSourceEffect;
import com.github.laxika.magicalvibes.model.effect.LibraryScope;
import com.github.laxika.magicalvibes.model.filter.PermanentIsTokenPredicate;

@CardRegistration(set = "TDC", collectorNumber = "6")
public class NerivCracklingVanguard extends Card {

    public NerivCracklingVanguard() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new CreateTokenEffect(2, "Goblin", 1, 1, CardColor.RED,
                        null, java.util.List.of(CardSubtype.GOBLIN)));

        addEffect(EffectSlot.ON_ATTACK,
                new ExileTopCardsToSourceEffect(
                        new DistinctPermanentNamesAmongControlled(
                                new PermanentIsTokenPredicate(), CountScope.CONTROLLER),
                        false, false, LibraryScope.CONTROLLER, false));

        addEffect(EffectSlot.STATIC,
                new ConditionalEffect(
                        new AttackedWithCommanderThisTurn(),
                        new AllowCastFromCardsExiledWithSourceEffect(
                                false, null, false, false, 0, null,
                                false, true, false)));
    }
}
