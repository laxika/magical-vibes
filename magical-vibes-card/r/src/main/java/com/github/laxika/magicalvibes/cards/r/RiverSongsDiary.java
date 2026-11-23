package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.condition.SourceExiledCardsThreshold;
import com.github.laxika.magicalvibes.model.effect.ExileTriggeringSpellEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringSpellExile;
import com.github.laxika.magicalvibes.model.effect.MayCastCardExiledWithSourceEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;
import java.util.List;

@CardRegistration(set = "WHO", collectorNumber = "182")
@CardRegistration(set = "WHO", collectorNumber = "460")
@CardRegistration(set = "WHO", collectorNumber = "787")
@CardRegistration(set = "WHO", collectorNumber = "1051")
public class RiverSongsDiary extends Card {

    public RiverSongsDiary() {
        CardAnyOfPredicate instantOrSorcery = new CardAnyOfPredicate(List.of(
                new CardTypePredicate(CardType.INSTANT),
                new CardTypePredicate(CardType.SORCERY)));
        addTrigger(Triggers.playerCastsSpell().withFilter(instantOrSorcery).castFrom(Zone.HAND),
                new ExileTriggeringSpellEffect(TriggeringSpellExile.TRACKED_BY_SOURCE, false));
        addTrigger(Triggers.upkeep(ActivePlayer.YOU),
                TriggerOptions.DEFAULT.withInterveningIf(new SourceExiledCardsThreshold(4)),
                MayCastCardExiledWithSourceEffect.randomSelection());
    }
}
