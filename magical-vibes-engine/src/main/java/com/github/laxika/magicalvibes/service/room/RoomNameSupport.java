package com.github.laxika.magicalvibes.service.room;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** Reads Room door names from the modal spell representation used by Room cards. */
public final class RoomNameSupport {

    private RoomNameSupport() {
    }

    public static Set<String> allDoorNames(Card card) {
        return card.getEffects(EffectSlot.SPELL).stream()
                .filter(ChooseOneEffect.class::isInstance)
                .map(ChooseOneEffect.class::cast)
                .findFirst()
                .map(modal -> modal.options().stream()
                        .map(ChooseOneEffect.ChooseOneOption::label)
                        .collect(java.util.stream.Collectors.toUnmodifiableSet()))
                .orElseGet(() -> card.getName() == null
                        ? Set.of()
                        : Set.of(card.getName()));
    }

    public static Set<String> unlockedDoorNames(Permanent permanent) {
        List<String> doorNames = permanent.getCard().getEffects(EffectSlot.SPELL).stream()
                .filter(ChooseOneEffect.class::isInstance)
                .map(ChooseOneEffect.class::cast)
                .findFirst()
                .map(modal -> modal.options().stream()
                        .map(ChooseOneEffect.ChooseOneOption::label)
                        .toList())
                .orElseGet(() -> permanent.getCard().getName() == null
                        ? List.of()
                        : List.of(permanent.getCard().getName()));

        Set<String> unlocked = new HashSet<>();
        for (int doorIndex = 0; doorIndex < doorNames.size(); doorIndex++) {
            if (permanent.isRoomDoorUnlocked(doorIndex)) {
                unlocked.add(doorNames.get(doorIndex));
            }
        }
        return Set.copyOf(unlocked);
    }
}
