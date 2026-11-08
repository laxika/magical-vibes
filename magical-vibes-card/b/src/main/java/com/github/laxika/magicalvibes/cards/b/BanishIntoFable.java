package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.condition.CastFromZone;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanent;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CopyTriggeringSpellEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsEnchantmentPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "ELD", collectorNumber = "325")
public class BanishIntoFable extends Card {

    public BanishIntoFable() {
        target(TargetFilters.nonlandPermanent())
                .addSpellEffect(ReturnToHandEffect.target())
                .addSpellEffect(new CreateTokenEffect(
                        1, "Knight", 2, 2, CardColor.WHITE,
                        List.of(CardSubtype.KNIGHT), Set.of(Keyword.VIGILANCE), Set.of()));

        // "When you cast this spell from your hand" is the event condition; "copy it if you control an artifact,
        // then copy it if you control an enchantment" is one ability whose two "if"s are checked as it resolves.
        addTrigger(Triggers.youCastThisSpell(),
                TriggerOptions.DEFAULT.withEventCondition(new CastFromZone(Zone.HAND)),
                SequenceEffect.of(
                        new ConditionalEffect(new ControlsPermanent(new PermanentIsArtifactPredicate()),
                                new CopyTriggeringSpellEffect()),
                        new ConditionalEffect(new ControlsPermanent(new PermanentIsEnchantmentPredicate()),
                                new CopyTriggeringSpellEffect())));
    }
}
