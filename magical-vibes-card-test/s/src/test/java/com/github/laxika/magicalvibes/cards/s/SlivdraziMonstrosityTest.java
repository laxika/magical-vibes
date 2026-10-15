package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.e.EldraziConscription;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HandOfEmrakul;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SlivdraziMonstrosity.class, SinewSliver.class, HandOfEmrakul.class, GrizzlyBears.class, EldraziConscription.class})
class SlivdraziMonstrosityTest extends BaseCardTest {

    @Test
    @DisplayName("Eldrazi become Slivers, and Slivers gain devoid and annihilator 1")
    void grantsSliverAbilities() {
        Permanent monstrosity = addCreatureReady(player1, new SlivdraziMonstrosity());
        Permanent sinewSliver = addCreatureReady(player1, new SinewSliver());
        Permanent handOfEmrakul = addCreatureReady(player1, new HandOfEmrakul());
        addCreatureReady(player2, new GrizzlyBears());

        assertThat(gqs.getEffectiveColors(gd, monstrosity)).isEmpty();
        assertThat(gqs.getEffectiveColors(gd, sinewSliver)).isEmpty();
        assertThat(gqs.getEffectivePower(gd, handOfEmrakul)).isEqualTo(8);
        assertThat(gqs.hasKeyword(gd, sinewSliver, Keyword.DEVOID)).isTrue();

        declareAttackers(player1, List.of(gd.playerBattlefields.get(player1.getId()).indexOf(sinewSliver)));
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Creates an Eldrazi Sliver token that sacrifices for colorless mana")
    void createsManaToken() {
        addCreatureReady(player1, new SlivdraziMonstrosity());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        Permanent token = findPermanent(player1, "Eldrazi Sliver");
        assertThat(gqs.hasKeyword(gd, token, Keyword.DEVOID)).isTrue();

        int tokenIndex = gd.playerBattlefields.get(player1.getId()).indexOf(token);
        harness.activateAbility(player1, tokenIndex, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        harness.assertNotOnBattlefield(player1, "Eldrazi Sliver");
    }

    @Test
    @DisplayName("Noncreature Eldrazi also become Slivers and gain devoid")
    void grantsAbilitiesToNoncreatureEldrazi() {
        addCreatureReady(player1, new SlivdraziMonstrosity());
        Permanent creature = addCreatureReady(player1, new HandOfEmrakul());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new EldraziConscription());
        aura.setAttachedTo(creature.getId());

        assertThat(gqs.hasEffectiveSubtype(gd, aura, CardSubtype.SLIVER)).isTrue();
        assertThat(gqs.hasEffectiveSubtype(gd, aura, CardSubtype.ELDRAZI)).isTrue();
        assertThat(gqs.hasKeyword(gd, aura, Keyword.DEVOID)).isTrue();
    }

    @Test
    @DisplayName("Slivdrazi Monstrosity grants annihilator to itself")
    void grantsAnnihilatorToItself() {
        addCreatureReady(player1, new SlivdraziMonstrosity());
        addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(List.of(0));
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Eldrazi retain their own annihilator and gain another instance")
    void addsAnnihilatorToExistingAnnihilator() {
        addCreatureReady(player1, new SlivdraziMonstrosity());
        Permanent attacker = addCreatureReady(player1, new HandOfEmrakul());
        Permanent first = addCreatureReady(player2, new GrizzlyBears());
        Permanent second = addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(attacker)));
        assertThat(gd.stack).hasSize(2);
        resolveAllTriggers();
        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        harness.handleMultiplePermanentsChosen(player2, List.of(first.getId()));
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(first, second);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Opposing Eldrazi and Slivers do not gain Slivdrazi's abilities")
    void doesNotAffectOpposingPermanents() {
        addCreatureReady(player1, new SlivdraziMonstrosity());
        Permanent sliver = addCreatureReady(player2, new SinewSliver());
        Permanent eldrazi = addCreatureReady(player2, new HandOfEmrakul());

        assertThat(gqs.hasKeyword(gd, sliver, Keyword.DEVOID)).isFalse();
        assertThat(gqs.getEffectiveColors(gd, sliver)).isNotEmpty();
        assertThat(gqs.hasEffectiveSubtype(gd, eldrazi, CardSubtype.SLIVER)).isFalse();

        declareAttackers(player2, List.of(0));
        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Slivdrazi Monstrosity");
    }

    @Test
    @DisplayName("Created tokens retain their mana ability after Slivdrazi leaves")
    void tokenManaAbilitySurvivesSourceRemoval() {
        Permanent source = addCreatureReady(player1, new SlivdraziMonstrosity());
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.activateAbility(player1, 0, 0, null, null);
        resolveAllTriggers();
        Permanent token = findPermanent(player1, "Eldrazi Sliver");

        gd.playerBattlefields.get(player1.getId()).remove(source);
        assertThat(gqs.hasKeyword(gd, token, Keyword.DEVOID)).isFalse();
        assertThat(gqs.getEffectiveColors(gd, token)).isEmpty();
        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        harness.assertNotOnBattlefield(player1, "Eldrazi Sliver");
        assertThat(gd.stack).isEmpty();
    }
}
