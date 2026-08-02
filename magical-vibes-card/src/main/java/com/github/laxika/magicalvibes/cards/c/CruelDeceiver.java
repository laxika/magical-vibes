package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.TopCardOfLibraryType;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyDamagedCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.GrantEffectToSourceUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsOfTargetLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.RevealTopCardOfOwnLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.TargetLibraryAction;

import java.util.List;

@CardRegistration(set = "CHK", collectorNumber = "106")
public class CruelDeceiver extends Card {

    public CruelDeceiver() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}",
                List.of(new LookAtTopCardsOfTargetLibraryEffect(1, TargetLibraryAction.LOOK_ONLY)),
                "{1}: Look at the top card of your library."));
        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}",
                List.of(
                        new RevealTopCardOfOwnLibraryEffect(),
                        new ConditionalEffect(
                                new TopCardOfLibraryType(CardType.LAND),
                                new GrantEffectToSourceUntilEndOfTurnEffect(
                                        EffectSlot.ON_ALLY_CREATURE_DEALS_DAMAGE_TO_CREATURE,
                                        new DestroyDamagedCreatureEffect()))),
                "{2}: Reveal the top card of your library. If it's a land card, this creature gains "
                        + "\"Whenever this creature deals damage to a creature, destroy that creature\" "
                        + "until end of turn. Activate only once each turn.",
                1));
    }
}
