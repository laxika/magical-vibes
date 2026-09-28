package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ForetellCast;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.condition.CastFromZone;
import com.github.laxika.magicalvibes.model.effect.ConditionalReplacementEffect;
import com.github.laxika.magicalvibes.model.effect.EachOpponentChoosesPermanentToDestroyEffect;
import com.github.laxika.magicalvibes.model.effect.MassDamageEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;

import java.util.List;

@CardRegistration(set = "FIC", collectorNumber = "62")
@CardRegistration(set = "FIC", collectorNumber = "121")
public class UltimateMagicMeteor extends Card {

    public UltimateMagicMeteor() {
        var artifactOrLand = new PermanentAnyOfPredicate(List.of(
                new PermanentIsArtifactPredicate(),
                new PermanentIsLandPredicate()));
        addEffect(EffectSlot.SPELL, new ConditionalReplacementEffect(
                new CastFromZone(Zone.EXILE),
                new MassDamageEffect(7),
                SequenceEffect.of(
                        new MassDamageEffect(7),
                        new EachOpponentChoosesPermanentToDestroyEffect(artifactOrLand))));
        addCastingOption(new ForetellCast("{5}{R}"));
    }
}
