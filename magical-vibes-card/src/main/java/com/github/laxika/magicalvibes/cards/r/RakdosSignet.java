package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;

import java.util.List;

@CardRegistration(set = "DIS", collectorNumber = "165")
@CardRegistration(set = "MM3", collectorNumber = "225")
@CardRegistration(set = "SLD", collectorNumber = "289")
@CardRegistration(set = "GK2", collectorNumber = "76")
@CardRegistration(set = "AA1", collectorNumber = "10")
@CardRegistration(set = "RVR", collectorNumber = "265")
@CardRegistration(set = "CMD", collectorNumber = "257")
@CardRegistration(set = "DSC", collectorNumber = "250")
@CardRegistration(set = "AFC", collectorNumber = "214")
@CardRegistration(set = "OTC", collectorNumber = "265")
@CardRegistration(set = "LCC", collectorNumber = "311")
@CardRegistration(set = "C20", collectorNumber = "249")
@CardRegistration(set = "C16", collectorNumber = "268")
@CardRegistration(set = "VOC", collectorNumber = "166")
@CardRegistration(set = "BRC", collectorNumber = "156")
@CardRegistration(set = "C17", collectorNumber = "221")
@CardRegistration(set = "SCD", collectorNumber = "272")
@CardRegistration(set = "CMA", collectorNumber = "226")
public class RakdosSignet extends Card {

    public RakdosSignet() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{1}",
                List.of(new AwardManaEffect(ManaColor.BLACK), new AwardManaEffect(ManaColor.RED)),
                "{1}, {T}: Add {B}{R}."
        ));
    }
}
