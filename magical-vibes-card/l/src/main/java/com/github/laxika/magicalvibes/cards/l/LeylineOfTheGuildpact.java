package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.effect.GrantActivatedAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.GrantColorEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.GrantSubtypeEffect;
import com.github.laxika.magicalvibes.model.effect.LeylineStartOnBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

import java.util.List;

@CardRegistration(set = "MKM", collectorNumber = "217")
@CardRegistration(set = "MKM", collectorNumber = "418")
public class LeylineOfTheGuildpact extends Card {

    public LeylineOfTheGuildpact() {
        addEffect(EffectSlot.ON_OPENING_HAND_REVEAL, new MayEffect(
                new LeylineStartOnBattlefieldEffect(),
                "Begin the game with Leyline of the Guildpact on the battlefield?"
        ));

        var nonlandPermanent = new PermanentNotPredicate(new PermanentIsLandPredicate());
        for (CardColor color : CardColor.values()) {
            addEffect(EffectSlot.STATIC,
                    new GrantColorEffect(color, GrantScope.OWN_PERMANENTS, false, nonlandPermanent));
        }

        for (CardSubtype basicLandType : List.of(CardSubtype.PLAINS, CardSubtype.ISLAND,
                CardSubtype.SWAMP, CardSubtype.MOUNTAIN, CardSubtype.FOREST)) {
            addEffect(EffectSlot.STATIC, new GrantSubtypeEffect(basicLandType, GrantScope.OWN_LANDS));
        }

        addEffect(EffectSlot.STATIC, new GrantActivatedAbilityEffect(
                ManaAbilities.tapForAnyColor(),
                GrantScope.OWN_PERMANENTS,
                new PermanentIsLandPredicate()
        ));
    }
}
