package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.h.HiddenHerbalists;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({IrontreadCrusher.class, HiddenHerbalists.class})
class IrontreadCrusherTest extends BaseCardTest {

    @Test
    void isNotACreatureBeforeCrewing() {
        Permanent crusher = addCreatureReady(player1, new IrontreadCrusher());

        assertThat(gqs.isCreature(gd, crusher)).isFalse();
    }

    @Test
    void crewWithEnoughPowerAnimatesCrusherAndTapsCrew() {
        Permanent crusher = addCreatureReady(player1, new IrontreadCrusher());
        Permanent crew = addCreatureReady(player1, new HiddenHerbalists());
        Permanent secondCrew = addCreatureReady(player1, new HiddenHerbalists());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(crusher.isAnimatedUntilEndOfTurn()).isTrue();
        assertThat(gqs.isCreature(gd, crusher)).isTrue();
        assertThat(gqs.getEffectivePower(gd, crusher)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, crusher)).isEqualTo(6);
        assertThat(crew.isTapped()).isTrue();
        assertThat(secondCrew.isTapped()).isTrue();
    }

    @Test
    void cannotCrewWithoutEnoughPower() {
        addCreatureReady(player1, new IrontreadCrusher());
        addCreatureReady(player1, new HiddenHerbalists());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough creature power to crew");
    }

    @Test
    void crewAnimationResetsAtEndOfTurn() {
        Permanent crusher = addCreatureReady(player1, new IrontreadCrusher());
        addCreatureReady(player1, new HiddenHerbalists());
        addCreatureReady(player1, new HiddenHerbalists());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(gqs.isCreature(gd, crusher)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(crusher.isAnimatedUntilEndOfTurn()).isFalse();
        assertThat(gqs.isCreature(gd, crusher)).isFalse();
    }

    @Test
    void newlyEnteredCreaturesCanCrewANewlyEnteredVehicle() {
        Permanent crusher = harness.addToBattlefieldAndReturn(player1, new IrontreadCrusher());
        Permanent firstCrew = harness.addToBattlefieldAndReturn(player1, new HiddenHerbalists());
        Permanent secondCrew = harness.addToBattlefieldAndReturn(player1, new HiddenHerbalists());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(firstCrew.isTapped()).isTrue();
        assertThat(secondCrew.isTapped()).isTrue();
        assertThat(gqs.isCreature(gd, crusher)).isTrue();
        assertThat(crusher.isTapped()).isFalse();
    }

    @Test
    void crewTapsCreaturesAsCostButAnimatesOnlyOnResolution() {
        Permanent crusher = addCreatureReady(player1, new IrontreadCrusher());
        Permanent firstCrew = addCreatureReady(player1, new HiddenHerbalists());
        Permanent secondCrew = addCreatureReady(player1, new HiddenHerbalists());

        harness.activateAbility(player1, 0, null, null);

        assertThat(firstCrew.isTapped()).isTrue();
        assertThat(secondCrew.isTapped()).isTrue();
        assertThat(gqs.isCreature(gd, crusher)).isFalse();
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, crusher)).isTrue();
        assertThat(crusher.isTapped()).isFalse();
    }

    @Test
    void tappedCreaturesCannotContributeToCrewPower() {
        addCreatureReady(player1, new IrontreadCrusher());
        Permanent availableCrew = addCreatureReady(player1, new HiddenHerbalists());
        Permanent tappedCrew = addCreatureReady(player1, new HiddenHerbalists());
        tappedCrew.tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough creature power to crew");

        assertThat(availableCrew.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void opponentsCreaturesCannotContributeToCrewPower() {
        addCreatureReady(player1, new IrontreadCrusher());
        Permanent ownCrew = addCreatureReady(player1, new HiddenHerbalists());
        Permanent opposingCrew = addCreatureReady(player2, new HiddenHerbalists());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough creature power to crew");

        assertThat(ownCrew.isTapped()).isFalse();
        assertThat(opposingCrew.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }
}
