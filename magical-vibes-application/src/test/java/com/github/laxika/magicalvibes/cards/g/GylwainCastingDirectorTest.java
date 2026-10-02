package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.m.MassProduction;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GylwainCastingDirector.class, GrizzlyBears.class, MassProduction.class})
class GylwainCastingDirectorTest extends BaseCardTest {

    private static final String ROYAL = "Create a Royal Role token attached to that creature.";
    private static final String SORCERER = "Create a Sorcerer Role token attached to that creature.";
    private static final String MONSTER = "Create a Monster Role token attached to that creature.";

    @Test
    @DisplayName("Gylwain's own entry can create a Royal Role attached to Gylwain")
    void ownEntryCreatesRoyalRole() {
        castGylwain();
        chooseRole(ROYAL);

        Permanent gylwain = findPermanent(player1, "Gylwain, Casting Director");
        Permanent role = findPermanent(player1, "Royal");
        assertThat(role.getAttachedTo()).isEqualTo(gylwain.getId());
        assertThat(gqs.getEffectivePower(gd, gylwain)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, gylwain)).isEqualTo(4);
    }

    @Test
    @DisplayName("Gylwain can create a Sorcerer Role attached to another nontoken creature")
    void anotherNontokenCreatureCreatesSorcererRole() {
        harness.addToBattlefield(player1, new GylwainCastingDirector());

        castGrizzlyBears();
        chooseRole(SORCERER);

        Permanent bears = findPermanent(player1, "Grizzly Bears");
        Permanent role = findPermanent(player1, "Sorcerer");
        assertThat(role.getAttachedTo()).isEqualTo(bears.getId());
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(3);
    }

    @Test
    @DisplayName("Gylwain can create a Monster Role that grants trample")
    void anotherNontokenCreatureCreatesMonsterRole() {
        harness.addToBattlefield(player1, new GylwainCastingDirector());

        castGrizzlyBears();
        chooseRole(MONSTER);

        Permanent bears = findPermanent(player1, "Grizzly Bears");
        Permanent role = findPermanent(player1, "Monster");
        assertThat(role.getAttachedTo()).isEqualTo(bears.getId());
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("Gylwain does not trigger for token creatures entering")
    void tokenEntryDoesNotTrigger() {
        harness.addToBattlefield(player1, new GylwainCastingDirector());

        harness.setHand(player1, List.of(new MassProduction()));
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Royal")).isEmpty();
        assertThat(findPermanents(player1, "Sorcerer")).isEmpty();
        assertThat(findPermanents(player1, "Monster")).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    private void castGylwain() {
        harness.setHand(player1, List.of(new GylwainCastingDirector()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
    }

    private void castGrizzlyBears() {
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
    }

    private void chooseRole(String role) {
        harness.handleListChoice(player1, role);
        harness.passBothPriorities();
    }
}
