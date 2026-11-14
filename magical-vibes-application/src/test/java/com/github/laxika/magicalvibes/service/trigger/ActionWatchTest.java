package com.github.laxika.magicalvibes.service.trigger;

import com.github.laxika.magicalvibes.model.AbilityZones;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectActionTrigger;
import com.github.laxika.magicalvibes.model.trigger.PlayerActionTrigger;
import com.github.laxika.magicalvibes.model.trigger.TriggerCondition;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class ActionWatchTest {

    @Test
    void everyWatchReadsItsOwnTriggerAndZone() {
        assertThat(Stream.of(ActionWatch.values(), SagaWatch.values(),
                        LibraryWatch.values())
                .flatMap(Arrays::stream)
                .map(watch -> Map.entry(watch.trigger(), watch.zone())))
                .doesNotHaveDuplicates();
    }

    @Test
    void everyActionWatchReadsAnActionTriggerInAZoneItFunctionsFrom() {
        assertThat(ActionWatch.values()).allSatisfy(watch -> {
            assertThat(watch.trigger()).as(watch.name())
                    .isInstanceOfAny(PlayerActionTrigger.class, ObjectActionTrigger.class);
            assertThat(watch.zone()).as(watch.name()).isEqualTo(watch.name().startsWith("GRAVEYARD_")
                    ? Zone.GRAVEYARD : watch == ActionWatch.ON_SELF_BECOMES_PLOTTED ? Zone.EXILE : Zone.BATTLEFIELD);
            assertThat(watch.trigger().defaultZones().functionsIn(watch.zone())
                    || watch.name().startsWith("GRAVEYARD_")).as(watch.name()).isTrue();
        });
    }

    @Test
    void aWatchReadsOnlyTheAbilitiesOfItsTriggerAndZone() {
        Card card = new Card();
        DrawCardEffect crime = new DrawCardEffect(1);
        GainLifeEffect crimeFromGraveyard = new GainLifeEffect(2);
        DrawCardEffect plotted = new DrawCardEffect(3);
        card.addTrigger(Triggers.youCommitCrime(), crime);
        card.addTrigger(new TriggerCondition(Triggers.youCommitCrime(), AbilityZones.GRAVEYARD), TriggerOptions.DEFAULT,
                crimeFromGraveyard);
        card.addTrigger(Triggers.becomesPlotted(), plotted);

        assertThat(ActionAbilityReader.printedEffects(card, ActionWatch.ON_CONTROLLER_COMMITS_CRIME))
                .containsExactly(crime);
        assertThat(ActionAbilityReader.printedEffects(card, ActionWatch.GRAVEYARD_ON_CONTROLLER_COMMITS_CRIME))
                .containsExactly(crimeFromGraveyard);
        assertThat(ActionAbilityReader.printedEffects(card, ActionWatch.ON_SELF_BECOMES_PLOTTED))
                .containsExactly(plotted);
        assertThat(ActionAbilityReader.printedEffects(card, ActionWatch.ON_OPPONENT_COMMITS_CRIME)).isEmpty();
    }
}
