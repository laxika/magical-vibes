package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.a.AmazingSpiderMan;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.TransformSelfEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "SPM", collectorNumber = "10")
public class PeterParker extends Card {

    public PeterParker() {
        AmazingSpiderMan backFace = new AmazingSpiderMan();
        setBackFaceCard(backFace);
        setModalDoubleFaced(true);

        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new CreateTokenEffect(
                "Spider", 2, 1, CardColor.GREEN,
                List.of(CardSubtype.SPIDER), Set.of(Keyword.REACH), Set.of()
        ));
        addActivatedAbility(new ActivatedAbility(
                false, "{1}{G}{W}{U}", List.of(new TransformSelfEffect()),
                "{1}{G}{W}{U}: Transform Peter Parker. Activate only as a sorcery."
        ));
        addEffect(EffectSlot.SPELL, new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption("Peter Parker", List.of())
                        .withManaCost("{1}{W}"),
                new ChooseOneEffect.ChooseOneOption("Amazing Spider-Man", backFace.getEffects(EffectSlot.SPELL))
                        .withManaCost("{1}{G}{W}{U}")
        )));
    }

    @Override
    public String getBackFaceClassName() {
        return "AmazingSpiderMan";
    }
}
