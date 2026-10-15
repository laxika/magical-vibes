package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardCast;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.ExileSpellEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;

import java.util.List;
import java.util.Map;
import java.util.Set;

@CardRegistration(set = "40K", collectorNumber = "63")
public class TheirNumberIsLegion extends Card {

    public TheirNumberIsLegion() {
        addCastingOption(new GraveyardCast());

        addEffect(EffectSlot.SPELL, new CreateTokenEffect(
                CardType.CREATURE, new XValue(), "Necron Warrior", 2, 2,
                CardColor.BLACK, null,
                List.of(CardSubtype.NECRON, CardSubtype.WARRIOR), Set.of(), Set.of(CardType.ARTIFACT),
                false, true, Map.of(), List.of(), false, false, false, 0, Set.of()));
        addEffect(EffectSlot.SPELL, new GainLifeEffect(new PermanentCount(
                new PermanentIsArtifactPredicate(), CountScope.CONTROLLER)));
        addEffect(EffectSlot.SPELL, new ExileSpellEffect());
    }
}
