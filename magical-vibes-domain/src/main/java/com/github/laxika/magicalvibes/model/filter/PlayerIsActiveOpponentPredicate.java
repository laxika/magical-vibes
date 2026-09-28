package com.github.laxika.magicalvibes.model.filter;

/** Restricts a player target to an opponent who is currently the active player. */
public record PlayerIsActiveOpponentPredicate() implements PlayerPredicate {
}
