package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.condition.TopCardOfLibraryType;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyDamagedCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.GrantEffectToSourceUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsOfTargetLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.LibraryOwner;
import com.github.laxika.magicalvibes.model.effect.RevealTopCardOfLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.TargetLibraryAction;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

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
                        new RevealTopCardOfLibraryEffect(LibraryOwner.CONTROLLER),
                        new ConditionalEffect(
                                new TopCardOfLibraryType(CardType.LAND),
                                new GrantEffectToSourceUntilEndOfTurnEffect(
                                        new TriggeredAbility(Triggers.creatureYouControlDealsDamageToCreature(),
                                        new DestroyDamagedCreatureEffect())))),
                "{2}: Reveal the top card of your library. If it's a land card, this creature gains "
                        + "\"Whenever this creature deals damage to a creature, destroy that creature\" "
                        + "until end of turn. Activate only once each turn.",
                1));
    }
}
