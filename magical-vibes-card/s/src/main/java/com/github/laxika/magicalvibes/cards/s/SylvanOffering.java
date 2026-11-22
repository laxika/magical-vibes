package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.ChoosePlayerEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.PlayerSubject;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import java.util.List;
import java.util.Set;

@CardRegistration(set = "SLD", collectorNumber = "1309")
@CardRegistration(set = "C14", collectorNumber = "48")
@CardRegistration(set = "NCC", collectorNumber = "314")
@CardRegistration(set = "LTC", collectorNumber = "261")
@CardRegistration(set = "CMA", collectorNumber = "150")
public class SylvanOffering extends Card {

    public SylvanOffering() {
        PlayerSubject youAndThatPlayer = Subjects.allOf(Subjects.you(), Subjects.playersThisWay(StepKind.CHOOSE));
        addSpellEffect(ChoosePlayerEffect.opponent());
        addSpellEffect(new CreateTokenEffect("Treefolk", new XValue(), new XValue(), CardColor.GREEN,
                List.of(CardSubtype.TREEFOLK), Set.of(), Set.of()).forController(youAndThatPlayer));
        addSpellEffect(ChoosePlayerEffect.opponent());
        addSpellEffect(new CreateTokenEffect(new XValue(), "Elf Warrior", 1, 1, CardColor.GREEN,
                List.of(CardSubtype.ELF, CardSubtype.WARRIOR), Set.of(), Set.of()).forController(youAndThatPlayer));
    }
}
