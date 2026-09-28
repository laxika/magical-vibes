package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.e.EbonPraetor;
import com.github.laxika.magicalvibes.cards.e.EleshNornMotherOfMachines;
import com.github.laxika.magicalvibes.cards.j.JinGitaxiasProgressTyrant;
import com.github.laxika.magicalvibes.cards.s.SheoldredTheApocalypse;
import com.github.laxika.magicalvibes.cards.u.UrabraskHereticPraetor;
import com.github.laxika.magicalvibes.cards.v.VorinclexMonstrousRaider;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfRandomCardEffect;

import java.util.List;
import java.util.function.Supplier;

@CardRegistration(set = "MB2", collectorNumber = "270")
@CardRegistration(set = "MB2", collectorNumber = "506")
public class WhosThatPraetor extends Card {

    private static final List<Supplier<? extends Card>> PRAETORS = List.of(
            EleshNornMotherOfMachines::new,
            JinGitaxiasProgressTyrant::new,
            SheoldredTheApocalypse::new,
            UrabraskHereticPraetor::new,
            VorinclexMonstrousRaider::new,
            EbonPraetor::new);

    public WhosThatPraetor() {
        addEffect(EffectSlot.SPELL, new CreateTokenCopyOfRandomCardEffect(PRAETORS));
    }
}
