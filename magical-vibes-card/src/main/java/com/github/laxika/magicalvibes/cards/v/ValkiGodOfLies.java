package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.t.TibaltCosmicImpostor;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BecomeCopyOfExiledCreatureWithManaValueEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.EachOpponentRevealsHandAndExilesCreatureCardEffect;

import java.util.List;

@CardRegistration(set = "KHM", collectorNumber = "114")
public class ValkiGodOfLies extends Card {

    public ValkiGodOfLies() {
        setBackFaceCard(new TibaltCosmicImpostor());
        setModalDoubleFaced(true);

        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new EachOpponentRevealsHandAndExilesCreatureCardEffect());
        addActivatedAbility(new ActivatedAbility(
                false, "{X}", List.of(new BecomeCopyOfExiledCreatureWithManaValueEffect()),
                "{X}: Choose a creature card exiled with Valki with mana value X. Valki becomes a copy of that card."
        ));
        addEffect(EffectSlot.SPELL, new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption("Valki, God of Lies", List.of())
                        .withManaCost("{1}{B}"),
                new ChooseOneEffect.ChooseOneOption("Tibalt, Cosmic Impostor", List.of())
                        .withManaCost("{5}{B}{R}")
        )));
    }

    @Override
    public String getBackFaceClassName() {
        return "TibaltCosmicImpostor";
    }
}
