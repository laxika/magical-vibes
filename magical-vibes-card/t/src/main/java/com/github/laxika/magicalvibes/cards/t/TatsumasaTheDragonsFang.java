package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.ExileSelfCost;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.ReturnExiledCardToBattlefieldUnderOwnerControlEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * The Dragon Spirit token carries the return as its own death trigger; the exiled Tatsumasa card is
 * bound to it when the token is created, and comes back under its owner's control.
 */
@CardRegistration(set = "CHK", collectorNumber = "270")
public class TatsumasaTheDragonsFang extends Card {

    public TatsumasaTheDragonsFang() {
        addEffect(EffectSlot.STATIC, new StaticBoostEffect(5, 5, GrantScope.EQUIPPED_CREATURE));
        addActivatedAbility(new ActivatedAbility(
                false, "{6}",
                List.of(new ExileSelfCost(), dragonSpiritToken()),
                "{6}, Exile Tatsumasa, the Dragon's Fang: Create a 5/5 blue Dragon Spirit creature "
                        + "token with flying. Return Tatsumasa to the battlefield under its owner's "
                        + "control when that token dies."));
        addActivatedAbility(new EquipActivatedAbility("{3}"));
    }

    private static CreateTokenEffect dragonSpiritToken() {
        Map<EffectSlot, CardEffect> tokenEffects =
                Map.of(EffectSlot.ON_DEATH, new ReturnExiledCardToBattlefieldUnderOwnerControlEffect(null));
        return new CreateTokenEffect(1, "Dragon Spirit", 5, 5, CardColor.BLUE,
                List.of(CardSubtype.DRAGON, CardSubtype.SPIRIT), Set.of(Keyword.FLYING),
                Set.<CardType>of(), tokenEffects);
    }
}
