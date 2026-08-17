package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.effect.CastAnyNumberOfEldraziSpellsFromOutsideGameEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeRecipient;
import com.github.laxika.magicalvibes.model.filter.PermanentTruePredicate;

import java.util.List;
import java.util.Map;
import java.util.Set;

@CardRegistration(set = "ROE", collectorNumber = "11")
public class SpawnsireOfUlamog extends Card {

    public SpawnsireOfUlamog() {
        addEffect(EffectSlot.ON_ATTACK, new SacrificePermanentsEffect(
                1, new PermanentTruePredicate(), SacrificeRecipient.DEFENDING_PLAYER));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{4}",
                List.of(new CreateTokenEffect(
                        CardType.CREATURE, 2, "Eldrazi Spawn", 0, 1, null, null,
                        List.of(CardSubtype.ELDRAZI, CardSubtype.SPAWN), Set.of(), Set.of(), false, false,
                        Map.of(), List.of(new ActivatedAbility(
                                false,
                                null,
                                List.of(new SacrificeSelfCost(), new AwardManaEffect(ManaColor.COLORLESS)),
                                "Sacrifice this token: Add {C}."
                        )), false, false, false, 0, Set.of())),
                "{4}: Create two 0/1 colorless Eldrazi Spawn creature tokens. They have \"Sacrifice this token: Add {C}.\""
        ));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{20}",
                List.of(new CastAnyNumberOfEldraziSpellsFromOutsideGameEffect()),
                "{20}: Cast any number of Eldrazi spells from among cards you own outside the game without paying their mana costs."
        ));
    }
}
