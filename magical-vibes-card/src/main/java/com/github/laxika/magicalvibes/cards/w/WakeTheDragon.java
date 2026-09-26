package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.FlashbackCast;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.GainControlOfPermanentDamagedPlayerControlsEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;

import java.util.List;
import java.util.Map;
import java.util.Set;

@CardRegistration(set = "LTC", collectorNumber = "74")
@CardRegistration(set = "LTC", collectorNumber = "154")
public class WakeTheDragon extends Card {

    public WakeTheDragon() {
        addEffect(EffectSlot.SPELL, new CreateTokenEffect(
                CardType.CREATURE, 1, "Dragon", 6, 6,
                CardColor.BLACK, Set.of(CardColor.BLACK, CardColor.RED),
                List.of(CardSubtype.DRAGON), Set.of(Keyword.FLYING, Keyword.MENACE), Set.of(),
                false, false,
                Map.of(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER,
                        new GainControlOfPermanentDamagedPlayerControlsEffect(
                                new PermanentIsArtifactPredicate())),
                List.of(), false, false, false, 0, Set.of()));
        addCastingOption(new FlashbackCast("{6}{B}{R}"));
    }
}
