package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.FlashbackCast;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.amount.GreatestManaValueAmongOwnedCommanders;
import com.github.laxika.magicalvibes.model.condition.CastFromZone;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.EachOpponentSacrificesPermanentCreateTokensEffect;
import com.github.laxika.magicalvibes.model.effect.ReduceOwnCastCostEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;

@CardRegistration(set = "MIC", collectorNumber = "36")
@CardRegistration(set = "MIC", collectorNumber = "74")
public class VisionsOfRuin extends Card {

    public VisionsOfRuin() {
        addEffect(EffectSlot.SPELL, new EachOpponentSacrificesPermanentCreateTokensEffect(
                new PermanentIsArtifactPredicate(), CreateTokenEffect.ofTreasureToken(1)));
        addEffect(EffectSlot.STATIC, new ConditionalEffect(
                new CastFromZone(Zone.GRAVEYARD),
                new ReduceOwnCastCostEffect(new GreatestManaValueAmongOwnedCommanders())));
        addCastingOption(new FlashbackCast("{8}{R}{R}"));
    }
}
