package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.RevealTopCardPermanentToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.ShuffleTargetPermanentIntoLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.ThenEffectRecipient;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "VMA", collectorNumber = "154")
@CardRegistration(set = "SLD", collectorNumber = "741")
@CardRegistration(set = "SLD", collectorNumber = "823")
@CardRegistration(set = "WHO", collectorNumber = "225")
@CardRegistration(set = "WHO", collectorNumber = "473")
@CardRegistration(set = "WHO", collectorNumber = "816")
@CardRegistration(set = "WHO", collectorNumber = "1064")
@CardRegistration(set = "2X2", collectorNumber = "105")
@CardRegistration(set = "STA", collectorNumber = "36")
@CardRegistration(set = "SOC", collectorNumber = "239")
@CardRegistration(set = "MSC", collectorNumber = "164")
@CardRegistration(set = "MSC", collectorNumber = "359")
@CardRegistration(set = "CMD", collectorNumber = "114")
@CardRegistration(set = "MAR", collectorNumber = "69")
@CardRegistration(set = "C14", collectorNumber = "174")
@CardRegistration(set = "PIP", collectorNumber = "189")
@CardRegistration(set = "PIP", collectorNumber = "466")
@CardRegistration(set = "PIP", collectorNumber = "717")
@CardRegistration(set = "PIP", collectorNumber = "994")
@CardRegistration(set = "FIC", collectorNumber = "291")
@CardRegistration(set = "MOC", collectorNumber = "273")
@CardRegistration(set = "40K", collectorNumber = "205")
@CardRegistration(set = "NCC", collectorNumber = "266")
@CardRegistration(set = "DSC", collectorNumber = "162")
@CardRegistration(set = "EOC", collectorNumber = "49")
@CardRegistration(set = "TDC", collectorNumber = "208")
@CardRegistration(set = "MKC", collectorNumber = "149")
@CardRegistration(set = "AFC", collectorNumber = "117")
@CardRegistration(set = "OTC", collectorNumber = "160")
@CardRegistration(set = "LCC", collectorNumber = "221")
@CardRegistration(set = "C20", collectorNumber = "146")
@CardRegistration(set = "BLC", collectorNumber = "115")
@CardRegistration(set = "C19", collectorNumber = "136")
@CardRegistration(set = "NEC", collectorNumber = "104")
@CardRegistration(set = "BRC", collectorNumber = "114")
@CardRegistration(set = "C18", collectorNumber = "122")
@CardRegistration(set = "C17", collectorNumber = "131")
@CardRegistration(set = "C16", collectorNumber = "122")
@CardRegistration(set = "DRC", collectorNumber = "48")
@CardRegistration(set = "SCD", collectorNumber = "127")
@CardRegistration(set = "FDC", collectorNumber = "146")
@CardRegistration(set = "CM2", collectorNumber = "89")
public class ChaosWarp extends Card {

    public ChaosWarp() {
        target(TargetFilters.permanent())
                .addEffect(EffectSlot.SPELL, new ShuffleTargetPermanentIntoLibraryEffect(
                        new RevealTopCardPermanentToBattlefieldEffect(),
                        ThenEffectRecipient.TARGET_OWNER));
    }
}
