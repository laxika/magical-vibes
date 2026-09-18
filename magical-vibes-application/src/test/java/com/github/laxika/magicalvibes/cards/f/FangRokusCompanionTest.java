package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.IsamaruHoundOfKonda;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FangRokusCompanion.class, GrizzlyBears.class, IsamaruHoundOfKonda.class})
class FangRokusCompanionTest extends BaseCardTest {

    @Test
    @DisplayName("Attack trigger targets another legendary creature you control")
    void attackTriggerTargetsAnotherLegendaryCreatureYouControl() {
        Permanent fang = addCreatureReady(player1, new FangRokusCompanion());
        Permanent legendary = addCreatureReady(player1, new IsamaruHoundOfKonda());
        Permanent opponentCreature = addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(List.of(0));

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).containsExactly(legendary.getId());
        assertThat(choice.validIds()).doesNotContain(fang.getId(), opponentCreature.getId());

        harness.handlePermanentChosen(player1, legendary.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, legendary)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, legendary)).isEqualTo(2);
    }

    @Test
    @DisplayName("Returns from death as a Spirit, but not after dying as a Spirit")
    void returnsOnceAsSpirit() {
        Permanent fang = addCreatureReady(player1, new FangRokusCompanion());

        kill(fang);

        Permanent returned = findPermanent(player1, "Fang, Roku's Companion");
        assertThat(returned.getGrantedSubtypes()).contains(CardSubtype.SPIRIT);
        harness.assertNotInGraveyard(player1, "Fang, Roku's Companion");

        kill(returned);

        harness.assertNotOnBattlefield(player1, "Fang, Roku's Companion");
        harness.assertInGraveyard(player1, "Fang, Roku's Companion");
    }

    private void kill(Permanent creature) {
        creature.setMarkedDamage(creature.getEffectiveToughness());
        harness.runStateBasedActions();
        harness.passBothPriorities();
    }
}
