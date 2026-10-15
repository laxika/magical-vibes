package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.condition.AllOf;
import com.github.laxika.magicalvibes.model.condition.CastFromZone;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanent;
import com.github.laxika.magicalvibes.model.effect.CopyThisSpellIfConditionEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsEnchantmentPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "ELD", collectorNumber = "325")
public class BanishIntoFable extends Card {

    public BanishIntoFable() {
        target(TargetFilters.nonlandPermanent())
                .addEffect(EffectSlot.SPELL, ReturnToHandEffect.target())
                .addEffect(EffectSlot.SPELL, new CreateTokenEffect(
                        1, "Knight", 2, 2, CardColor.WHITE,
                        List.of(CardSubtype.KNIGHT), Set.of(Keyword.VIGILANCE), Set.of()));

        addEffect(EffectSlot.ON_SELF_CAST, new CopyThisSpellIfConditionEffect(new AllOf(List.of(
                new CastFromZone(Zone.HAND),
                new ControlsPermanent(new PermanentIsArtifactPredicate())))));
        addEffect(EffectSlot.ON_SELF_CAST, new CopyThisSpellIfConditionEffect(new AllOf(List.of(
                new CastFromZone(Zone.HAND),
                new ControlsPermanent(new PermanentIsEnchantmentPredicate())))));
    }
}
