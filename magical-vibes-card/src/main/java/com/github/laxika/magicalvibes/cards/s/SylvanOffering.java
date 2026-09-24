package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.TargetOpponentCreatesTokenEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "SLD", collectorNumber = "1309")
public class SylvanOffering extends Card {

    public SylvanOffering() {
        CreateTokenEffect treefolk = new CreateTokenEffect(
                "Treefolk", new XValue(), new XValue(), CardColor.GREEN,
                List.of(CardSubtype.TREEFOLK), Set.of(), Set.of());
        addEffect(EffectSlot.SPELL, treefolk);
        addEffect(EffectSlot.SPELL, new TargetOpponentCreatesTokenEffect(treefolk));

        CreateTokenEffect elfWarrior = new CreateTokenEffect(
                new XValue(), "Elf Warrior", 1, 1, CardColor.GREEN,
                List.of(CardSubtype.ELF, CardSubtype.WARRIOR), Set.of(), Set.of());
        addEffect(EffectSlot.SPELL, elfWarrior);
        addEffect(EffectSlot.SPELL, new TargetOpponentCreatesTokenEffect(elfWarrior));
    }
}
