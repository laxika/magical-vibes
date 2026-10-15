package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CantBlockEffect;
import com.github.laxika.magicalvibes.model.effect.ProtectionFromSubtypesEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenForTargetPlayerEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardForTargetPlayerEffect;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;

import java.util.List;
import java.util.Map;
import java.util.Set;

@CardRegistration(set = "40K", collectorNumber = "114")
public class CyberneticaDatasmith extends Card {

    public CyberneticaDatasmith() {
        addEffect(EffectSlot.STATIC, new ProtectionFromSubtypesEffect(Set.of(CardSubtype.ROBOT)));
        PlayerPredicateTargetFilter playerTarget = new PlayerPredicateTargetFilter(
                new PlayerRelationPredicate(PlayerRelation.ANY),
                "Target must be a player");
        CreateTokenEffect robotToken = new CreateTokenEffect(
                1,
                "Robot",
                4,
                4,
                null,
                List.of(CardSubtype.ROBOT),
                Set.of(),
                Set.of(CardType.ARTIFACT),
                Map.of(EffectSlot.STATIC, new CantBlockEffect()));

        addActivatedAbility(new ActivatedAbility(
                true,
                "{U}",
                List.of(
                        DrawCardForTargetPlayerEffect.forTargetGroup(1, 0),
                        CreateTokenForTargetPlayerEffect.forTargetGroup(robotToken, 1)),
                "Field Reprogramming — {U}, {T}: Target player draws a card. Another target player creates a 4/4 colorless Robot artifact creature token with \"This token can't block.\"",
                List.of(playerTarget, playerTarget),
                2,
                2));
    }
}
