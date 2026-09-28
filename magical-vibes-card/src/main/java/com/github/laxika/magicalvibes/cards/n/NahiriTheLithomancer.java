package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokensAndAttachEquipmentEffect;
import com.github.laxika.magicalvibes.model.effect.PutCardFromHandOrGraveyardOntoBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;

import java.util.List;
import java.util.Map;
import java.util.Set;

@CardRegistration(set = "CMM", collectorNumber = "45")
@CardRegistration(set = "CMM", collectorNumber = "467")
@CardRegistration(set = "C14", collectorNumber = "10")
public class NahiriTheLithomancer extends Card {

    public NahiriTheLithomancer() {
        CreateTokenEffect korSoldier = new CreateTokenEffect(
                "Kor Soldier", 1, 1, CardColor.WHITE,
                List.of(CardSubtype.KOR, CardSubtype.SOLDIER), Set.of(), Set.of());
        addActivatedAbility(new ActivatedAbility(
                +2,
                List.of(new CreateTokensAndAttachEquipmentEffect(korSoldier)),
                "+2: Create a 1/1 white Kor Soldier creature token. You may attach an Equipment you control to it."
        ));

        addActivatedAbility(new ActivatedAbility(
                -2,
                List.of(new PutCardFromHandOrGraveyardOntoBattlefieldEffect(
                        new CardSubtypePredicate(CardSubtype.EQUIPMENT), "Equipment")),
                "−2: You may put an Equipment card from your hand or graveyard onto the battlefield."
        ));

        CreateTokenEffect stoneforgedBlade = new CreateTokenEffect(
                CardType.ARTIFACT, 1, "Stoneforged Blade", 0, 0,
                null, null, List.of(CardSubtype.EQUIPMENT), Set.of(Keyword.INDESTRUCTIBLE), Set.of(),
                false, false,
                Map.of(EffectSlot.STATIC,
                        new StaticBoostEffect(5, 5, Set.of(Keyword.DOUBLE_STRIKE), GrantScope.EQUIPPED_CREATURE)),
                List.of(new EquipActivatedAbility("{0}")),
                false, false, false, 0, Set.of());
        addActivatedAbility(new ActivatedAbility(
                -10,
                List.of(stoneforgedBlade),
                "−10: Create a colorless Equipment artifact token named Stoneforged Blade. It has indestructible, "
                        + "\"Equipped creature gets +5/+5 and has double strike,\" and equip {0}."
        ));
    }
}
