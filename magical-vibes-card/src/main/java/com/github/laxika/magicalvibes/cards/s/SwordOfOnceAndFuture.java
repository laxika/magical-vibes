package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.effect.ChooseCardFromGraveyardAndGrantCastPermissionEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.ProtectionFromColorsEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.effect.SurveilEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardMaxManaValuePredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "MOM", collectorNumber = "265")
public class SwordOfOnceAndFuture extends Card {

    private static final CardMaxManaValuePredicate MAX_MANA_VALUE_TWO = new CardMaxManaValuePredicate(2);

    private static final CardAnyOfPredicate INSTANT_OR_SORCERY_WITH_MAX_MANA_VALUE_TWO = new CardAnyOfPredicate(List.of(
            new CardAllOfPredicate(List.of(new CardTypePredicate(CardType.INSTANT), MAX_MANA_VALUE_TWO)),
            new CardAllOfPredicate(List.of(new CardTypePredicate(CardType.SORCERY), MAX_MANA_VALUE_TWO))));

    public SwordOfOnceAndFuture() {
        addEffect(EffectSlot.STATIC, new StaticBoostEffect(2, 2, GrantScope.EQUIPPED_CREATURE));
        addEffect(EffectSlot.STATIC, new ProtectionFromColorsEffect(
                Set.of(CardColor.BLUE, CardColor.BLACK), GrantScope.EQUIPPED_CREATURE));
        addEffect(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER, new SurveilEffect(2));
        addEffect(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER, new MayEffect(
            new ChooseCardFromGraveyardAndGrantCastPermissionEffect(
                    INSTANT_OR_SORCERY_WITH_MAX_MANA_VALUE_TWO,
                    GraveyardSearchScope.CONTROLLERS_GRAVEYARD, true, true),
                "Cast an instant or sorcery spell with mana value 2 or less from your graveyard without paying its mana cost?"));
        addActivatedAbility(new EquipActivatedAbility("{2}"));
    }
}
