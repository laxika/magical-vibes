package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.effect.CounterUnlessPaysEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTwoPermanentsThenSearchLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "WHO", collectorNumber = "103")
@CardRegistration(set = "WHO", collectorNumber = "708")
public class FugitiveOfTheJudoon extends Card {

    public FugitiveOfTheJudoon() {
        CreateTokenEffect humanToken = new CreateTokenEffect(
                CardType.CREATURE, 1, "Human", 1, 1, CardColor.WHITE, null,
                List.of(CardSubtype.HUMAN), Set.of(Keyword.WARD), Set.of(), false, false,
                List.of(new TriggeredAbility(Triggers.becomesTargetOfOpponentSpellOrAbility(),
                        new CounterUnlessPaysEffect(2))),
                List.of(), false, false, false, 0, Set.of());
        CreateTokenEffect rhinoToken = new CreateTokenEffect(
                CardType.CREATURE, 1, "Alien Rhino", 4, 4, CardColor.WHITE, null,
                List.of(CardSubtype.ALIEN, CardSubtype.RHINO), Set.of(), Set.of(), false, false,
                List.of(), List.of(), false, false, false, 0, Set.of());

        addTrigger(Triggers.chapter(1), SequenceEffect.of(humanToken, rhinoToken));
        addTrigger(Triggers.chapter(2), CreateTokenEffect.ofClueToken(1));
        addTrigger(Triggers.chapter(3), new MayEffect(
                new ExileTwoPermanentsThenSearchLibraryEffect(
                        new PermanentHasSubtypePredicate(CardSubtype.HUMAN), "Human",
                        new PermanentIsArtifactPredicate(), "artifact",
                        new CardSubtypePredicate(CardSubtype.DOCTOR)),
                "Exile a Human you control and an artifact you control?"));
    }
}
