package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.l.LostVale;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenForTargetPlayerEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.effect.TransformSelfEffect;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "XLN", collectorNumber = "235")
public class DowsingDagger extends Card {

    public DowsingDagger() {
        // Set up back face
        LostVale backFace = new LostVale();
        backFace.setSetCode(getSetCode());
        backFace.setCollectorNumber(getCollectorNumber());
        setBackFaceCard(backFace);

        // When Dowsing Dagger enters the battlefield, target opponent creates
        // two 0/2 green Plant creature tokens with defender.
        target(new PlayerPredicateTargetFilter(
                new PlayerRelationPredicate(PlayerRelation.OPPONENT),
                "Target must be an opponent"
        )).addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new CreateTokenForTargetPlayerEffect(
                new CreateTokenEffect(2, "Plant", 0, 2,
                        CardColor.GREEN, List.of(CardSubtype.PLANT),
                        Set.of(Keyword.DEFENDER), Set.of())
        ));

        // Equipped creature gets +2/+1.
        addEffect(EffectSlot.STATIC, new StaticBoostEffect(2, 1, GrantScope.EQUIPPED_CREATURE));

        // Whenever equipped creature deals combat damage to a player, you may transform
        // Dowsing Dagger.
        addEffect(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER,
                new MayEffect(new TransformSelfEffect(), "Transform Dowsing Dagger into Lost Vale?"));

        // Equip {2}
        addActivatedAbility(new EquipActivatedAbility("{2}"));
    }

    @Override
    public String getBackFaceClassName() {
        return "LostVale";
    }
}
