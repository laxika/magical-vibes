package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardAndDrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;

import java.util.List;
import java.util.Map;
import java.util.Set;

@CardRegistration(set = "ACR", collectorNumber = "38")
public class TheSpearOfLeonidas extends Card {

    public TheSpearOfLeonidas() {
        CreateTokenEffect phobos = new CreateTokenEffect(
                CardType.CREATURE, 1, "Phobos", 3, 2, CardColor.RED, null,
                List.of(CardSubtype.HORSE), Set.of(), Set.of(), false, false,
                Map.of(), List.of(), false, false, true, 0, Set.of());

        addEffect(EffectSlot.ON_ATTACK, new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Bull Rush — It gains double strike until end of turn",
                        new GrantKeywordEffect(Keyword.DOUBLE_STRIKE, GrantScope.EQUIPPED_CREATURE)),
                new ChooseOneEffect.ChooseOneOption(
                        "Summon — Create Phobos, a legendary 3/2 red Horse creature token",
                        phobos),
                new ChooseOneEffect.ChooseOneOption(
                        "Revelation — Discard two cards, then draw two cards",
                        new DiscardAndDrawCardEffect(2, 2))
        )));

        addActivatedAbility(new EquipActivatedAbility("{2}"));
    }
}
