package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.AllOf;
import com.github.laxika.magicalvibes.model.condition.Condition;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanent;
import com.github.laxika.magicalvibes.model.effect.AttachMatchingEquipmentToCreatedPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNamedPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

import java.util.List;
import java.util.Map;
import java.util.Set;

@CardRegistration(set = "5DN", collectorNumber = "131")
public class HelmOfKaldra extends Card {

    private static final PermanentPredicate KALDRA_EQUIPMENT = new PermanentAllOfPredicate(List.of(
            new PermanentHasSubtypePredicate(CardSubtype.EQUIPMENT),
            new PermanentAnyOfPredicate(List.of(
                    new PermanentNamedPredicate("Helm of Kaldra"),
                    new PermanentNamedPredicate("Sword of Kaldra"),
                    new PermanentNamedPredicate("Shield of Kaldra"))),
            new PermanentControlledBySourceControllerPredicate()));

    private static final Condition CONTROLS_KALDRA_EQUIPMENT = new AllOf(List.of(
            new ControlsPermanent(kaldraEquipmentNamed("Helm of Kaldra")),
            new ControlsPermanent(kaldraEquipmentNamed("Sword of Kaldra")),
            new ControlsPermanent(kaldraEquipmentNamed("Shield of Kaldra"))));

    public HelmOfKaldra() {
        addEffect(EffectSlot.STATIC,
                new GrantKeywordEffect(Keyword.FIRST_STRIKE, GrantScope.EQUIPPED_CREATURE));
        addEffect(EffectSlot.STATIC,
                new GrantKeywordEffect(Keyword.TRAMPLE, GrantScope.EQUIPPED_CREATURE));
        addEffect(EffectSlot.STATIC,
                new GrantKeywordEffect(Keyword.HASTE, GrantScope.EQUIPPED_CREATURE));

        CreateTokenEffect kaldra = new CreateTokenEffect(
                CardType.CREATURE, 1, "Kaldra", 4, 4, null, null,
                List.of(CardSubtype.AVATAR), Set.of(), Set.of(), false, false,
                Map.of(), List.of(), false, false, true, 0, Set.of());
        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}",
                List.of(new ConditionalEffect(CONTROLS_KALDRA_EQUIPMENT,
                        SequenceEffect.of(kaldra,
                                new AttachMatchingEquipmentToCreatedPermanentEffect(KALDRA_EQUIPMENT)))),
                "{1}: If you control Equipment named Helm of Kaldra, Sword of Kaldra, and Shield of Kaldra, "
                        + "create Kaldra, a legendary 4/4 colorless Avatar creature token. Attach those Equipment to it."));

        addActivatedAbility(new EquipActivatedAbility("{2}"));
    }

    private static PermanentPredicate kaldraEquipmentNamed(String name) {
        return new PermanentAllOfPredicate(List.of(
                new PermanentHasSubtypePredicate(CardSubtype.EQUIPMENT),
                new PermanentNamedPredicate(name)));
    }
}
