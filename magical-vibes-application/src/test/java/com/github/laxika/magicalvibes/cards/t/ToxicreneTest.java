package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.a.AshayaSoulOfTheWild;
import com.github.laxika.magicalvibes.cards.d.DunesOfTheDead;
import com.github.laxika.magicalvibes.cards.g.GoForTheThroat;
import com.github.laxika.magicalvibes.cards.h.HostileDesert;
import com.github.laxika.magicalvibes.cards.s.StoneRain;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Toxicrene.class, HostileDesert.class, DunesOfTheDead.class, StoneRain.class,
        GoForTheThroat.class, AshayaSoulOfTheWild.class})
class ToxicreneTest extends BaseCardTest {

    @Test
    void allLandsCanProduceAnyColor() {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new HostileDesert());
        resolveToxicrene();

        harness.activateAbility(player2, 0, null, null);
        harness.handleListChoice(player2, "RED");

        assertThat(land.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.RED)).isEqualTo(1);
    }

    @Test
    void landsLoseTheirOtherAbilities() {
        harness.addToBattlefield(player2, new DunesOfTheDead());
        resolveToxicrene();

        harness.setHand(player1, List.of(new StoneRain()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.castAndResolveSorcery(player1, 0, harness.getPermanentId(player2, "Dunes of the Dead"));

        assertThat(findPermanents(player2, "Zombie")).isEmpty();
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player2, "Dunes of the Dead");
    }

    @Test
    void replacesPrintedManaAbilitiesRatherThanPreservingColorlessMana() {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new DunesOfTheDead());
        resolveToxicrene();

        harness.activateAbility(player2, 0, null, null);

        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.COLORLESS)).isZero();
        harness.handleListChoice(player2, "BLUE");
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(land.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void affectsOwnLandsEnteringLaterAndAllowsEveryColor() {
        resolveToxicrene();
        List<ManaColor> colors = List.of(ManaColor.WHITE, ManaColor.BLUE, ManaColor.BLACK,
                ManaColor.RED, ManaColor.GREEN);

        for (int i = 0; i < colors.size(); i++) {
            Permanent land = harness.addToBattlefieldAndReturn(player1, new HostileDesert());
            harness.activateAbility(player1, i + 1, null, null);
            harness.handleListChoice(player1, colors.get(i).name());

            assertThat(land.isTapped()).isTrue();
            assertThat(gd.playerManaPools.get(player1.getId()).get(colors.get(i))).isEqualTo(1);
        }
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
    }

    @Test
    void landAbilitiesReturnWhenToxicreneLeaves() {
        harness.addToBattlefield(player2, new DunesOfTheDead());
        resolveToxicrene();
        harness.setHand(player1, List.of(new GoForTheThroat(), new StoneRain()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player1, "Toxicrene"));
        harness.assertInGraveyard(player1, "Toxicrene");

        harness.activateAbility(player2, 0, null, null);
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);

        harness.addMana(player1, ManaColor.RED, 3);
        harness.castAndResolveSorcery(player1, 0, harness.getPermanentId(player2, "Dunes of the Dead"));
        harness.passBothPriorities();

        assertThat(findPermanents(player2, "Zombie")).hasSize(1);
    }

    @Test
    void toxicreneAlsoLosesItsOwnAbilitiesWhenAshayaMakesItALand() {
        Permanent ashaya = harness.addToBattlefieldAndReturn(player1, new AshayaSoulOfTheWild());
        ashaya.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        resolveToxicrene();
        Permanent toxicrene = gqs.findPermanentById(gd, harness.getPermanentId(player1, "Toxicrene"));

        assertThat(gqs.isLand(gd, toxicrene)).isTrue();
        assertThat(gqs.hasKeyword(gd, toxicrene, Keyword.REACH)).isFalse();
        assertThat(gqs.hasKeyword(gd, toxicrene, Keyword.DEATHTOUCH)).isFalse();

        toxicrene.setSummoningSick(false);
        harness.activateAbility(player1, 1, null, null);
        harness.handleListChoice(player1, "RED");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
    }

    private void resolveToxicrene() {
        harness.setHand(player1, List.of(new Toxicrene()));
        harness.addMana(player1, ManaColor.GREEN, 4);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
    }
}
