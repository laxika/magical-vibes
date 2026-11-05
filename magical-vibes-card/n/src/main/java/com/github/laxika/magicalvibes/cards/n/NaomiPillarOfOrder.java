package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.AllOf;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanent;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsEnchantmentPredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;

@CardRegistration(set = "NEO", collectorNumber = "229")
public class NaomiPillarOfOrder extends Card {

    public NaomiPillarOfOrder() {
        TriggerOptions artifactAndEnchantment = TriggerOptions.DEFAULT.withInterveningIf(new AllOf(List.of(
                new ControlsPermanent(new PermanentIsArtifactPredicate()),
                new ControlsPermanent(new PermanentIsEnchantmentPredicate()))));
        CreateTokenEffect createSamurai = (
                new CreateTokenEffect(
                        1,
                        "Samurai",
                        2,
                        2,
                        CardColor.WHITE,
                        List.of(CardSubtype.SAMURAI),
                        Set.of(Keyword.VIGILANCE),
                        Set.of()));
        addTrigger(Triggers.enters(ObjectRelation.THIS), artifactAndEnchantment, createSamurai);
        addTrigger(Triggers.attacks(), artifactAndEnchantment, createSamurai);
    }
}
