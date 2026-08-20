package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.k.KaldringTheRimestaff;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSupertypePredicate;

import java.util.List;

@CardRegistration(set = "KHM", collectorNumber = "179")
public class JornGodOfWinter extends Card {

    public JornGodOfWinter() {
        setBackFaceCard(new KaldringTheRimestaff());
        setModalDoubleFaced(true);

        addEffect(EffectSlot.ON_ATTACK, new UntapPermanentsEffect(
                TapUntapScope.CONTROLLED,
                new PermanentHasSupertypePredicate(CardSupertype.SNOW)));

        addEffect(EffectSlot.SPELL, new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption("Jorn, God of Winter", List.of())
                        .withManaCost("{2}{G}"),
                new ChooseOneEffect.ChooseOneOption("Kaldring, the Rimestaff", List.of())
                        .withManaCost("{1}{U}{B}")
        )));
    }

    @Override
    public String getBackFaceClassName() {
        return "KaldringTheRimestaff";
    }
}
