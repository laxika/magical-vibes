package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.n.NamelessInversion;
import com.github.laxika.magicalvibes.cards.p.ProwessOfTheFair;
import com.github.laxika.magicalvibes.cards.w.WoodlandChangeling;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ElvishPromenade.class, LlanowarElves.class, GrizzlyBears.class,
        WoodlandChangeling.class, ProwessOfTheFair.class, NamelessInversion.class})
class ElvishPromenadeTest extends BaseCardTest {

    @Test
    @DisplayName("Creates one Elf Warrior token for each Elf controlled")
    void createsTokenPerElf() {
        harness.addToBattlefield(player1, new LlanowarElves());
        harness.addToBattlefield(player1, new LlanowarElves());
        harness.addToBattlefield(player1, new GrizzlyBears()); // not an Elf
        harness.setHand(player1, List.of(new ElvishPromenade()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castAndResolveSorcery(player1, 0, 0);

        GameData gd = harness.getGameData();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(p -> p.getCard().getSubtypes().contains(CardSubtype.WARRIOR))
                .hasSize(2)
                .allMatch(p -> p.getCard().getPower() == 1 && p.getCard().getToughness() == 1
                        && p.getCard().getSubtypes().contains(CardSubtype.ELF));
    }

    @Test
    @DisplayName("Creates no tokens when no Elves are controlled")
    void createsNoTokensWithoutElves() {
        harness.addToBattlefield(player1, new GrizzlyBears()); // not an Elf
        harness.setHand(player1, List.of(new ElvishPromenade()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castAndResolveSorcery(player1, 0, 0);

        GameData gd = harness.getGameData();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getSubtypes().contains(CardSubtype.WARRIOR));
    }

    @Test
    @DisplayName("Counts changelings and noncreature Elves, but not opposing Elves")
    void countsAllControlledElfPermanents() {
        harness.addToBattlefield(player1, new WoodlandChangeling());
        harness.addToBattlefield(player1, new ProwessOfTheFair());
        harness.addToBattlefield(player2, new WoodlandChangeling());
        harness.addToBattlefield(player2, new ProwessOfTheFair());
        harness.setHand(player1, List.of(new ElvishPromenade()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(4);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(p -> p.getCard().getName().equals("Elf Warrior"))
                .hasSize(2)
                .allSatisfy(p -> {
                    assertThat(p.getCard().isToken()).isTrue();
                    assertThat(p.getCard().hasType(CardType.CREATURE)).isTrue();
                    assertThat(p.getCard().getColor()).isEqualTo(CardColor.GREEN);
                    assertThat(p.getCard().getSubtypes())
                            .containsExactlyInAnyOrder(CardSubtype.ELF, CardSubtype.WARRIOR);
                    assertThat(p.getCard().getPower()).isEqualTo(1);
                    assertThat(p.getCard().getToughness()).isEqualTo(1);
                    assertThat(p.isTapped()).isFalse();
                });
        assertThat(gd.playerBattlefields.get(player2.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Counts tokens from an earlier resolution without counting newly created tokens again")
    void countsExistingElfTokens() {
        harness.addToBattlefield(player1, new WoodlandChangeling());
        harness.setHand(player1, List.of(new ElvishPromenade(), new ElvishPromenade()));
        harness.addMana(player1, ManaColor.GREEN, 8);

        harness.castAndResolveSorcery(player1, 0, 0);
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(2);
        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(4);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(p -> p.getCard().getName().equals("Elf Warrior"))
                .hasSize(3);
    }

    @Test
    @DisplayName("Counts Elves at resolution after an Elf is removed in response")
    void countsElvesAtResolution() {
        var elf = harness.addToBattlefieldAndReturn(player1, new WoodlandChangeling());
        harness.setHand(player1, List.of(new ElvishPromenade()));
        harness.setHand(player2, List.of(new NamelessInversion()));
        harness.addMana(player1, ManaColor.GREEN, 4);
        harness.addMana(player2, ManaColor.BLACK, 2);

        harness.castSorcery(player1, 0, 0);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, elf.getId());
        harness.assertInGraveyard(player1, "Woodland Changeling");
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Elvish Promenade");
    }
}
