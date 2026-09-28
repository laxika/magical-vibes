package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.ExileDyingCreatureCardAndCreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnExiledCardToBattlefieldUnderOwnerControlEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfThenEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringCardConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;

import java.util.List;
import java.util.Map;
import java.util.Set;

@CardRegistration(set = "LCC", collectorNumber = "210")
public class TimotharBaronOfBats extends Card {

    public TimotharBaronOfBats() {
        addEffect(EffectSlot.ON_ALLY_NONTOKEN_CREATURE_DIES, new TriggeringCardConditionalEffect(
                new CardSubtypePredicate(CardSubtype.VAMPIRE),
                new MayPayManaEffect("{1}",
                        new ExileDyingCreatureCardAndCreateTokenEffect(batToken()),
                        "Pay {1} to exile that card and create a Bat?")));
    }

    private static CreateTokenEffect batToken() {
        return new CreateTokenEffect(
                1,
                "Bat",
                1,
                1,
                CardColor.BLACK,
                List.of(CardSubtype.BAT),
                Set.of(Keyword.FLYING),
                Set.<CardType>of(),
                Map.of(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER,
                        new SacrificeSelfThenEffect(
                                new ReturnExiledCardToBattlefieldUnderOwnerControlEffect(null, true))));
    }
}
