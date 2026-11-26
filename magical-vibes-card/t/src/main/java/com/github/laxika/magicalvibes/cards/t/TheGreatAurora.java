package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.AmountThisWay;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.EachPlayerMayPutLandsFromHandToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.EachPlayerShufflesZonesIntoLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.ExileSpellEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;

/**
 * The Great Aurora: each player shuffles their hand and all permanents they own into their library,
 * then draws that many cards; each player may then put any number of land cards from their hand
 * onto the battlefield; finally the spell exiles itself instead of going to the graveyard.
 */
@CardRegistration(set = "ORI", collectorNumber = "179")
@CardRegistration(set = "PIO", collectorNumber = "380")
public class TheGreatAurora extends Card {

    public TheGreatAurora() {
        addSpellEffect(SequenceEffect.of(
                EachPlayerShufflesZonesIntoLibraryEffect.handsAndOwnedPermanents(),
                DrawCardEffect.eachEvaluatedFor(AmountThisWay.shuffledAwayByYou(), Subjects.eachPlayer())));
        addSpellEffect(new EachPlayerMayPutLandsFromHandToBattlefieldEffect());
        addSpellEffect(new ExileSpellEffect());
    }
}
