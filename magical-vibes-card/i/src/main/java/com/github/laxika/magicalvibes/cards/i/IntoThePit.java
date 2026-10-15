package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CastingCost;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.SacrificePermanentsCost;
import com.github.laxika.magicalvibes.model.effect.AllowCastFromTopOfLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardOfOwnLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "DSC", collectorNumber = "20")
@CardRegistration(set = "DSC", collectorNumber = "50")
public class IntoThePit extends Card {

    public IntoThePit() {
        addEffect(EffectSlot.STATIC, new LookAtTopCardOfOwnLibraryEffect());
        addEffect(EffectSlot.STATIC, new AllowCastFromTopOfLibraryEffect(
                Set.of(
                        CardType.CREATURE,
                        CardType.ENCHANTMENT,
                        CardType.SORCERY,
                        CardType.INSTANT,
                        CardType.ARTIFACT,
                        CardType.PLANESWALKER,
                        CardType.BATTLE,
                        CardType.KINDRED
                ),
                List.<CastingCost>of(new SacrificePermanentsCost(
                        1, new PermanentNotPredicate(new PermanentIsLandPredicate())))));
    }
}
